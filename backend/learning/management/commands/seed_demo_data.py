from django.contrib.auth import get_user_model
from django.core.management.base import BaseCommand

from learning.models import BoardScan, Chapter, Module, Subject, Tag
from studytools.models import Difficulty, Flashcard, Quiz, QuizQuestion, SourceType, Summary


class Command(BaseCommand):
    help = "Create beginner-friendly ModuleLens sample data for local testing."

    def add_arguments(self, parser):
        parser.add_argument("--username", default="demo_student")
        parser.add_argument("--password", default="demo-password-123")

    def handle(self, *args, **options):
        User = get_user_model()
        username = options["username"]
        password = options["password"]

        user, created = User.objects.get_or_create(
            username=username,
            defaults={
                "email": f"{username}@example.com",
                "first_name": "Demo",
                "last_name": "Student",
            },
        )
        if created:
            user.set_password(password)
            user.save()

        subject, _ = Subject.objects.update_or_create(
            owner=user,
            title="Native Android Development",
            defaults={
                "description": "Kotlin, Jetpack Compose, CameraX, and Android app architecture.",
                "color": "#2563EB",
            },
        )

        kotlin_module, _ = Module.objects.update_or_create(
            owner=user,
            subject=subject,
            title="Kotlin Basics",
            defaults={
                "description": "Variables, functions, classes, null safety, and collections.",
                "content_type": Module.ContentType.MARKDOWN,
                "markdown_content": (
                    "# Kotlin Basics\n\n"
                    "Kotlin is a modern language used for Android development. "
                    "Important topics include val, var, functions, classes, and null safety."
                ),
            },
        )
        compose_module, _ = Module.objects.update_or_create(
            owner=user,
            subject=subject,
            title="Jetpack Compose Basics",
            defaults={
                "description": "Declarative UI, composables, state, recomposition, and Material 3.",
                "content_type": Module.ContentType.MARKDOWN,
                "markdown_content": (
                    "# Jetpack Compose Basics\n\n"
                    "Compose builds UI using composable functions. State changes cause recomposition."
                ),
            },
        )

        Chapter.objects.update_or_create(
            owner=user,
            module=kotlin_module,
            order=1,
            defaults={
                "title": "Variables and Functions",
                "markdown_content": "Use val for read-only values and var for mutable values.",
            },
        )
        state_chapter, _ = Chapter.objects.update_or_create(
            owner=user,
            module=compose_module,
            order=1,
            defaults={
                "title": "State Management",
                "markdown_content": "remember stores state during recomposition. rememberSaveable survives configuration changes.",
            },
        )

        important_tag, _ = Tag.objects.update_or_create(
            owner=user,
            name="important",
            defaults={"color": "#F59E0B"},
        )

        board_scan, _ = BoardScan.objects.update_or_create(
            owner=user,
            subject=subject,
            module=compose_module,
            chapter=state_chapter,
            raw_ocr_text="remember vs rememberSaveable. State hoisting moves state to parent composable.",
            defaults={
                "cleaned_text": (
                    "remember keeps state during recomposition. rememberSaveable keeps state after "
                    "configuration changes. State hoisting means moving state to a parent composable."
                ),
                "review_status": BoardScan.ReviewStatus.NEEDS_REVIEW,
            },
        )
        board_scan.tags.set([important_tag])

        Summary.objects.update_or_create(
            owner=user,
            module=compose_module,
            chapter=state_chapter,
            source_type=SourceType.CHAPTER,
            defaults={
                "content": (
                    "Compose state controls what the UI displays. remember keeps a value during recomposition, "
                    "while rememberSaveable also survives configuration changes."
                ),
                "is_ai_generated": True,
            },
        )

        Flashcard.objects.update_or_create(
            owner=user,
            module=compose_module,
            chapter=state_chapter,
            question="What does remember do in Jetpack Compose?",
            defaults={
                "answer": "It keeps state during recomposition.",
                "source_type": SourceType.CHAPTER,
                "difficulty": Difficulty.MEDIUM,
                "is_ai_generated": True,
            },
        )

        quiz, _ = Quiz.objects.update_or_create(
            owner=user,
            module=compose_module,
            chapter=state_chapter,
            title="Compose State Quick Check",
            defaults={
                "description": "Short quiz about remember and state hoisting.",
                "source_type": SourceType.CHAPTER,
                "is_ai_generated": True,
            },
        )
        QuizQuestion.objects.update_or_create(
            quiz=quiz,
            order=1,
            defaults={
                "question": "Which API can preserve state across configuration changes?",
                "question_type": QuizQuestion.QuestionType.MULTIPLE_CHOICE,
                "choices": ["remember", "rememberSaveable", "Column", "Modifier"],
                "correct_answer": "rememberSaveable",
                "explanation": "rememberSaveable saves state that can survive activity recreation.",
            },
        )

        self.stdout.write(self.style.SUCCESS(f"Demo data ready for user '{username}' with password '{password}'.")) 
