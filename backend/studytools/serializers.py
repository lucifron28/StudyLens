from rest_framework import serializers

from learning.models import BoardScan, Chapter, Module
from studytools.models import Flashcard, Quiz, QuizAttempt, QuizQuestion, SourceType, Summary


class OwnedRelationMixin:
    owned_relation_fields: dict[str, type] = {}

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        request = self.context.get("request")
        if not request or not request.user.is_authenticated:
            return

        for field_name, model_class in self.owned_relation_fields.items():
            if field_name not in self.fields:
                continue
            queryset = model_class.objects.filter(owner=request.user)
            field = self.fields[field_name]
            if hasattr(field, "queryset"):
                field.queryset = queryset
            elif hasattr(field, "child_relation") and hasattr(field.child_relation, "queryset"):
                field.child_relation.queryset = queryset


class SourceValidationMixin:
    def validate(self, attrs):
        instance = self.instance
        module = attrs.get("module", instance.module if instance else None)
        chapter = attrs.get("chapter", instance.chapter if instance else None)
        board_scan = attrs.get("board_scan", instance.board_scan if instance else None)

        if chapter:
            if module and chapter.module_id != module.id:
                raise serializers.ValidationError({"chapter": "Chapter must belong to the selected module."})
            if not module:
                attrs["module"] = chapter.module
                module = chapter.module

        if board_scan:
            if module and board_scan.module_id and board_scan.module_id != module.id:
                raise serializers.ValidationError({"board_scan": "Board scan must belong to the selected module."})
            if chapter and board_scan.chapter_id and board_scan.chapter_id != chapter.id:
                raise serializers.ValidationError({"board_scan": "Board scan must belong to the selected chapter."})
            if not module and board_scan.module:
                attrs["module"] = board_scan.module
            if not chapter and board_scan.chapter:
                attrs["chapter"] = board_scan.chapter

        return attrs


class SummarySerializer(SourceValidationMixin, OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)

    owned_relation_fields = {"module": Module, "chapter": Chapter, "board_scan": BoardScan}

    class Meta:
        model = Summary
        fields = [
            "id",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "board_scan",
            "source_type",
            "content",
            "is_ai_generated",
            "created_at",
        ]
        read_only_fields = ["id", "module_title", "chapter_title", "created_at"]


class FlashcardSerializer(SourceValidationMixin, OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)

    owned_relation_fields = {"module": Module, "chapter": Chapter, "board_scan": BoardScan}

    class Meta:
        model = Flashcard
        fields = [
            "id",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "board_scan",
            "question",
            "answer",
            "source_type",
            "is_ai_generated",
            "difficulty",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "module_title", "chapter_title", "created_at", "updated_at"]


class QuizQuestionSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    owned_relation_fields = {"quiz": Quiz}

    class Meta:
        model = QuizQuestion
        fields = [
            "id",
            "quiz",
            "question",
            "question_type",
            "choices",
            "correct_answer",
            "explanation",
            "order",
        ]
        read_only_fields = ["id"]

    def validate(self, attrs):
        question_type = attrs.get("question_type", self.instance.question_type if self.instance else None)
        choices = attrs.get("choices", self.instance.choices if self.instance else None)

        if question_type == QuizQuestion.QuestionType.MULTIPLE_CHOICE:
            if not isinstance(choices, list) or len(choices) < 2:
                raise serializers.ValidationError({"choices": "Multiple choice questions need at least two choices."})
        return attrs


class QuizSerializer(SourceValidationMixin, OwnedRelationMixin, serializers.ModelSerializer):
    module_title = serializers.CharField(source="module.title", read_only=True)
    chapter_title = serializers.CharField(source="chapter.title", read_only=True)
    questions = QuizQuestionSerializer(many=True, read_only=True)

    owned_relation_fields = {"module": Module, "chapter": Chapter, "board_scan": BoardScan}

    class Meta:
        model = Quiz
        fields = [
            "id",
            "module",
            "module_title",
            "chapter",
            "chapter_title",
            "board_scan",
            "title",
            "description",
            "source_type",
            "is_ai_generated",
            "questions",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["id", "module_title", "chapter_title", "questions", "created_at", "updated_at"]


class QuizAttemptSerializer(OwnedRelationMixin, serializers.ModelSerializer):
    quiz_title = serializers.CharField(source="quiz.title", read_only=True)

    owned_relation_fields = {"quiz": Quiz}

    class Meta:
        model = QuizAttempt
        fields = [
            "id",
            "quiz",
            "quiz_title",
            "score",
            "total_questions",
            "answers",
            "completed_at",
            "created_at",
        ]
        read_only_fields = ["id", "quiz_title", "created_at"]

    def validate_quiz(self, quiz: Quiz) -> Quiz:
        request = self.context.get("request")
        if request and quiz.owner_id != request.user.id:
            raise serializers.ValidationError("Quiz does not belong to the current user.")
        return quiz

    def validate(self, attrs):
        score = attrs.get("score", self.instance.score if self.instance else 0)
        total = attrs.get("total_questions", self.instance.total_questions if self.instance else 0)
        if total and score > total:
            raise serializers.ValidationError({"score": "Score cannot be greater than total_questions."})
        return attrs


def source_model_fields(source) -> dict:
    return {
        "module": source.module,
        "chapter": source.chapter,
        "board_scan": source.board_scan,
        "source_type": source.source_type or SourceType.COMBINED,
    }

