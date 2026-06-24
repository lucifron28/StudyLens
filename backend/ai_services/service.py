import json
from dataclasses import dataclass

from django.conf import settings
from django.db import transaction
from rest_framework import serializers

from ai_services.models import TutorMessage, TutorSession
from ai_services.prompts import (
    FLASHCARD_SYSTEM_PROMPT,
    QUIZ_SYSTEM_PROMPT,
    SUMMARY_SYSTEM_PROMPT,
    TUTOR_CHECK_SYSTEM_PROMPT,
    TUTOR_START_SYSTEM_PROMPT,
    flashcard_user_prompt,
    quiz_user_prompt,
    source_user_prompt,
)
from ai_services.providers.base import AIProviderError, BaseAIProvider
from ai_services.providers.deepseek_provider import DeepSeekProvider
from ai_services.providers.ollama_provider import OllamaProvider
from learning.models import BoardScan, Chapter, Module
from studytools.models import Difficulty, Flashcard, Quiz, QuizQuestion, SourceType, Summary


MAX_SOURCE_CHARS = 12000


class AIOutputError(Exception):
    """Raised when AI output cannot be safely parsed or saved."""


@dataclass
class SourceBundle:
    title: str
    text: str
    source_type: str
    module: Module | None = None
    chapter: Chapter | None = None
    board_scan: BoardScan | None = None


def get_provider() -> BaseAIProvider:
    provider_name = getattr(settings, "AI_PROVIDER", "ollama").lower()
    if provider_name == "ollama":
        return OllamaProvider(
            base_url=getattr(settings, "OLLAMA_BASE_URL", "http://host.docker.internal:11434"),
            model=getattr(settings, "OLLAMA_MODEL", "qwen3.5:4b"),
        )
    if provider_name == "deepseek":
        return DeepSeekProvider(
            api_key=getattr(settings, "DEEPSEEK_API_KEY", ""),
            model=getattr(settings, "DEEPSEEK_MODEL", ""),
        )
    raise AIProviderError(f"Unsupported AI_PROVIDER '{provider_name}'. Use 'ollama' or 'deepseek'.")


def truncate_text(text: str, limit: int = MAX_SOURCE_CHARS) -> str:
    if len(text) <= limit:
        return text
    return text[:limit] + "\n\n[Text truncated for AI input limit.]"


def resolve_source(user, data: dict, allow_text: bool = True) -> SourceBundle:
    module = None
    chapter = None
    board_scan = None
    text_parts: list[str] = []
    title_parts: list[str] = []

    module_id = data.get("module_id")
    chapter_id = data.get("chapter_id")
    board_scan_id = data.get("board_scan_id")
    direct_text = (data.get("text") or "").strip()

    if module_id:
        module = Module.objects.filter(owner=user, id=module_id).first()
        if not module:
            raise serializers.ValidationError({"module_id": "Module not found."})
        text_parts.append("\n".join(part for part in [module.markdown_content, module.extracted_text] if part))
        title_parts.append(module.title)

    if chapter_id:
        chapter = Chapter.objects.select_related("module").filter(owner=user, id=chapter_id).first()
        if not chapter:
            raise serializers.ValidationError({"chapter_id": "Chapter not found."})
        if module and chapter.module_id != module.id:
            raise serializers.ValidationError({"chapter_id": "Chapter must belong to the selected module."})
        module = module or chapter.module
        text_parts.append("\n".join(part for part in [chapter.markdown_content, chapter.extracted_text] if part))
        title_parts.append(chapter.title)

    if board_scan_id:
        board_scan = BoardScan.objects.select_related("module", "chapter").filter(owner=user, id=board_scan_id).first()
        if not board_scan:
            raise serializers.ValidationError({"board_scan_id": "Board scan not found."})
        if module and board_scan.module_id and board_scan.module_id != module.id:
            raise serializers.ValidationError({"board_scan_id": "Board scan must belong to the selected module."})
        if chapter and board_scan.chapter_id and board_scan.chapter_id != chapter.id:
            raise serializers.ValidationError({"board_scan_id": "Board scan must belong to the selected chapter."})
        module = module or board_scan.module
        chapter = chapter or board_scan.chapter
        text_parts.append("\n".join(part for part in [board_scan.cleaned_text, board_scan.raw_ocr_text] if part))
        title_parts.append("Board scan")

    if direct_text:
        if not allow_text:
            raise serializers.ValidationError({"text": "Text is not supported for this endpoint."})
        text_parts.append(direct_text)
        title_parts.append("Provided text")

    combined_text = "\n\n".join(part.strip() for part in text_parts if part and part.strip())
    if not combined_text:
        raise serializers.ValidationError("Provide source text or an owned module, chapter, or board scan with text.")

    selected_count = sum(bool(value) for value in [module_id, chapter_id, board_scan_id, direct_text])
    if board_scan and selected_count == 1:
        source_type = SourceType.BOARD_SCAN
    elif chapter and selected_count == 1:
        source_type = SourceType.CHAPTER
    elif module and selected_count == 1:
        source_type = SourceType.MODULE
    else:
        source_type = SourceType.COMBINED

    title = " + ".join(title_parts) if title_parts else "StudyLens source"
    return SourceBundle(
        title=title,
        text=truncate_text(combined_text),
        source_type=source_type,
        module=module,
        chapter=chapter,
        board_scan=board_scan,
    )


def parse_json_output(raw_output: str):
    text = raw_output.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        if lines and lines[0].startswith("```"):
            lines = lines[1:]
        if lines and lines[-1].startswith("```"):
            lines = lines[:-1]
        text = "\n".join(lines).strip()

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        starts = [index for index in [text.find("{"), text.find("[")] if index != -1]
        if not starts:
            raise AIOutputError("AI returned invalid JSON.")
        start = min(starts)
        end = max(text.rfind("}"), text.rfind("]"))
        if end <= start:
            raise AIOutputError("AI returned incomplete JSON.")
        try:
            return json.loads(text[start : end + 1])
        except json.JSONDecodeError as exc:
            raise AIOutputError(f"AI returned invalid JSON: {exc.msg}") from exc


def source_fields(source: SourceBundle) -> dict:
    return {
        "module": source.module,
        "chapter": source.chapter,
        "board_scan": source.board_scan,
        "source_type": source.source_type,
    }


def create_summary(user, data: dict) -> Summary:
    source = resolve_source(user, data)
    provider = get_provider()
    content = provider.generate_text(
        messages=[{"role": "user", "content": source_user_prompt(source.title, source.text)}],
        system_prompt=SUMMARY_SYSTEM_PROMPT,
    ).strip()
    if not content:
        raise AIOutputError("AI returned an empty summary.")
    return Summary.objects.create(owner=user, content=content, is_ai_generated=True, **source_fields(source))


def _validated_flashcards(parsed, count: int) -> list[dict]:
    if not isinstance(parsed, list):
        raise AIOutputError("Flashcard output must be a JSON array.")

    valid_cards = []
    valid_difficulties = {choice.value for choice in Difficulty}
    for index, item in enumerate(parsed[:count], start=1):
        if not isinstance(item, dict):
            raise AIOutputError(f"Flashcard #{index} must be an object.")
        question = str(item.get("question", "")).strip()
        answer = str(item.get("answer", "")).strip()
        difficulty = str(item.get("difficulty", Difficulty.MEDIUM)).strip().lower()
        if not question or not answer:
            raise AIOutputError(f"Flashcard #{index} needs both question and answer.")
        if difficulty not in valid_difficulties:
            raise AIOutputError(f"Flashcard #{index} has invalid difficulty '{difficulty}'.")
        valid_cards.append({"question": question, "answer": answer, "difficulty": difficulty})

    if not valid_cards:
        raise AIOutputError("AI did not return any valid flashcards.")
    return valid_cards


def create_flashcards(user, data: dict) -> list[Flashcard]:
    count = data.get("count", 5)
    source = resolve_source(user, data)
    provider = get_provider()
    raw_output = provider.generate_text(
        messages=[{"role": "user", "content": flashcard_user_prompt(source.title, source.text, count)}],
        system_prompt=FLASHCARD_SYSTEM_PROMPT,
    )
    cards = _validated_flashcards(parse_json_output(raw_output), count)

    flashcards = [
        Flashcard(
            owner=user,
            question=card["question"],
            answer=card["answer"],
            difficulty=card["difficulty"],
            is_ai_generated=True,
            **source_fields(source),
        )
        for card in cards
    ]
    return Flashcard.objects.bulk_create(flashcards)


def _validated_quiz(parsed, count: int, requested_question_type: str | None = None) -> dict:
    if not isinstance(parsed, dict):
        raise AIOutputError("Quiz output must be a JSON object.")

    title = str(parsed.get("title", "")).strip() or "Generated Quiz"
    description = str(parsed.get("description", "")).strip()
    questions = parsed.get("questions")
    if not isinstance(questions, list) or not questions:
        raise AIOutputError("Quiz output must include a questions array.")

    valid_types = {choice.value for choice in QuizQuestion.QuestionType}
    validated_questions = []
    for index, item in enumerate(questions[:count], start=1):
        if not isinstance(item, dict):
            raise AIOutputError(f"Quiz question #{index} must be an object.")
        question = str(item.get("question", "")).strip()
        question_type = str(item.get("question_type", requested_question_type or "")).strip()
        choices = item.get("choices")
        correct_answer = str(item.get("correct_answer", "")).strip()
        explanation = str(item.get("explanation", "")).strip()

        if requested_question_type and question_type != requested_question_type:
            raise AIOutputError(f"Quiz question #{index} did not use requested type '{requested_question_type}'.")
        if question_type not in valid_types:
            raise AIOutputError(f"Quiz question #{index} has invalid question_type '{question_type}'.")
        if not question or not correct_answer:
            raise AIOutputError(f"Quiz question #{index} needs question and correct_answer.")
        if question_type == QuizQuestion.QuestionType.MULTIPLE_CHOICE:
            if not isinstance(choices, list) or len(choices) < 2:
                raise AIOutputError(f"Quiz question #{index} needs at least two choices.")
        else:
            choices = choices if isinstance(choices, list) else None

        validated_questions.append(
            {
                "question": question,
                "question_type": question_type,
                "choices": choices,
                "correct_answer": correct_answer,
                "explanation": explanation,
                "order": index,
            }
        )

    return {"title": title, "description": description, "questions": validated_questions}


def create_quiz(user, data: dict) -> Quiz:
    count = data.get("count", 5)
    question_type = data.get("question_type") or None
    source = resolve_source(user, data)
    provider = get_provider()
    raw_output = provider.generate_text(
        messages=[{"role": "user", "content": quiz_user_prompt(source.title, source.text, count, question_type)}],
        system_prompt=QUIZ_SYSTEM_PROMPT,
    )
    quiz_data = _validated_quiz(parse_json_output(raw_output), count, question_type)

    with transaction.atomic():
        quiz = Quiz.objects.create(
            owner=user,
            title=quiz_data["title"],
            description=quiz_data["description"],
            is_ai_generated=True,
            **source_fields(source),
        )
        QuizQuestion.objects.bulk_create([QuizQuestion(quiz=quiz, **item) for item in quiz_data["questions"]])
    return quiz


def start_tutor_session(user, data: dict) -> tuple[TutorSession, TutorMessage]:
    source = resolve_source(user, data, allow_text=False)
    provider = get_provider()
    raw_output = provider.generate_text(
        messages=[{"role": "user", "content": source_user_prompt(source.title, source.text)}],
        system_prompt=TUTOR_START_SYSTEM_PROMPT,
    )
    parsed = parse_json_output(raw_output)
    if not isinstance(parsed, dict) or not str(parsed.get("message", "")).strip():
        raise AIOutputError("Tutor start output must include a message.")

    with transaction.atomic():
        session = TutorSession.objects.create(
            owner=user,
            title=data.get("title") or f"Tutor: {source.title}",
            module=source.module,
            chapter=source.chapter,
            board_scan=source.board_scan,
        )
        message = TutorMessage.objects.create(
            session=session,
            role=TutorMessage.Role.ASSISTANT,
            content=str(parsed["message"]).strip(),
        )
    return session, message


def _conversation_for_tutor(session: TutorSession, source: SourceBundle) -> str:
    history = session.messages.order_by("created_at", "id")
    lines = [
        f"Source: {source.title}",
        source.text,
        "",
        f"Clear answers: {session.clear_answers_count}/{session.target_clear_answers}",
        "Conversation:",
    ]
    for message in history:
        clarity = f" ({message.clarity_result})" if message.clarity_result else ""
        lines.append(f"{message.role}{clarity}: {message.content}")
    return "\n".join(lines)


def create_tutor_reply(user, data: dict) -> tuple[TutorSession, TutorMessage]:
    session = TutorSession.objects.filter(owner=user, id=data["session_id"]).first()
    if not session:
        raise serializers.ValidationError({"session_id": "Tutor session not found."})
    if session.status in {TutorSession.Status.MASTERED, TutorSession.Status.ABANDONED}:
        raise serializers.ValidationError({"session_id": f"Session is already {session.status}."})
    session_updated_at = session.updated_at

    source = resolve_source(
        user,
        {
            "module_id": session.module_id,
            "chapter_id": session.chapter_id,
            "board_scan_id": session.board_scan_id,
        },
        allow_text=False,
    )
    provider = get_provider()
    raw_output = provider.generate_text(
        messages=[{"role": "user", "content": _conversation_for_tutor(session, source)}],
        system_prompt=TUTOR_CHECK_SYSTEM_PROMPT,
    )
    parsed = parse_json_output(raw_output)
    if not isinstance(parsed, dict):
        raise AIOutputError("Tutor response must be a JSON object.")

    clarity_result = str(parsed.get("clarity_result", "")).strip().lower()
    if clarity_result not in {choice.value for choice in TutorMessage.ClarityResult}:
        raise AIOutputError("Tutor response has invalid clarity_result.")
    assistant_content = str(parsed.get("message", "")).strip()
    if not assistant_content:
        raise AIOutputError("Tutor response must include a message.")

    with transaction.atomic():
        session = (
            TutorSession.objects.select_for_update()
            .filter(owner=user, id=data["session_id"])
            .first()
        )
        if not session:
            raise serializers.ValidationError({"session_id": "Tutor session not found."})
        if session.updated_at != session_updated_at:
            raise serializers.ValidationError({"session_id": "Session changed while the reply was generated. Try again."})

        user_message = TutorMessage.objects.create(
            session=session,
            role=TutorMessage.Role.USER,
            content=data["message"].strip(),
        )
        user_message.clarity_result = clarity_result
        user_message.save(update_fields=["clarity_result"])

        if clarity_result == TutorMessage.ClarityResult.CLEAR:
            session.clear_answers_count += 1
        if session.clear_answers_count >= session.target_clear_answers:
            session.status = TutorSession.Status.MASTERED
        elif clarity_result == TutorMessage.ClarityResult.UNCLEAR:
            session.status = TutorSession.Status.NEEDS_REVIEW
        else:
            session.status = TutorSession.Status.IN_PROGRESS
        session.save(update_fields=["clear_answers_count", "status", "updated_at"])

        assistant_message = TutorMessage.objects.create(
            session=session,
            role=TutorMessage.Role.ASSISTANT,
            content=assistant_content,
        )
    return session, assistant_message
