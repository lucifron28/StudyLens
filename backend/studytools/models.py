from django.conf import settings
from django.db import models

from learning.models import BoardScan, Chapter, Module


class SourceType(models.TextChoices):
    MODULE = "module", "Module"
    CHAPTER = "chapter", "Chapter"
    BOARD_SCAN = "board_scan", "Board scan"
    COMBINED = "combined", "Combined"
    MANUAL = "manual", "Manual"


class Difficulty(models.TextChoices):
    EASY = "easy", "Easy"
    MEDIUM = "medium", "Medium"
    HARD = "hard", "Hard"


class Summary(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="summaries")
    module = models.ForeignKey(Module, on_delete=models.SET_NULL, related_name="summaries", blank=True, null=True)
    chapter = models.ForeignKey(Chapter, on_delete=models.SET_NULL, related_name="summaries", blank=True, null=True)
    board_scan = models.ForeignKey(BoardScan, on_delete=models.SET_NULL, related_name="summaries", blank=True, null=True)
    source_type = models.CharField(max_length=20, choices=SourceType.choices, default=SourceType.MANUAL)
    content = models.TextField()
    is_ai_generated = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["owner", "source_type"]),
            models.Index(fields=["owner", "module"]),
            models.Index(fields=["owner", "chapter"]),
        ]

    def __str__(self) -> str:
        return f"Summary #{self.pk}"


class Flashcard(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="flashcards")
    module = models.ForeignKey(Module, on_delete=models.SET_NULL, related_name="flashcards", blank=True, null=True)
    chapter = models.ForeignKey(Chapter, on_delete=models.SET_NULL, related_name="flashcards", blank=True, null=True)
    board_scan = models.ForeignKey(BoardScan, on_delete=models.SET_NULL, related_name="flashcards", blank=True, null=True)
    question = models.TextField()
    answer = models.TextField()
    source_type = models.CharField(max_length=20, choices=SourceType.choices, default=SourceType.MANUAL)
    is_ai_generated = models.BooleanField(default=False)
    difficulty = models.CharField(max_length=20, choices=Difficulty.choices, default=Difficulty.MEDIUM)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["owner", "difficulty"]),
            models.Index(fields=["owner", "source_type"]),
            models.Index(fields=["owner", "module"]),
        ]

    def __str__(self) -> str:
        return self.question[:80]


class Quiz(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="quizzes")
    module = models.ForeignKey(Module, on_delete=models.SET_NULL, related_name="quizzes", blank=True, null=True)
    chapter = models.ForeignKey(Chapter, on_delete=models.SET_NULL, related_name="quizzes", blank=True, null=True)
    board_scan = models.ForeignKey(BoardScan, on_delete=models.SET_NULL, related_name="quizzes", blank=True, null=True)
    title = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    source_type = models.CharField(max_length=20, choices=SourceType.choices, default=SourceType.MANUAL)
    is_ai_generated = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["owner", "source_type"]),
            models.Index(fields=["owner", "module"]),
        ]

    def __str__(self) -> str:
        return self.title


class QuizQuestion(models.Model):
    class QuestionType(models.TextChoices):
        MULTIPLE_CHOICE = "multiple_choice", "Multiple choice"
        TRUE_FALSE = "true_false", "True/false"
        SHORT_ANSWER = "short_answer", "Short answer"

    quiz = models.ForeignKey(Quiz, on_delete=models.CASCADE, related_name="questions")
    question = models.TextField()
    question_type = models.CharField(
        max_length=30,
        choices=QuestionType.choices,
        default=QuestionType.MULTIPLE_CHOICE,
    )
    choices = models.JSONField(blank=True, null=True)
    correct_answer = models.TextField()
    explanation = models.TextField(blank=True)
    order = models.PositiveIntegerField(default=0)

    class Meta:
        ordering = ["order", "id"]
        indexes = [
            models.Index(fields=["quiz", "order"]),
        ]

    def __str__(self) -> str:
        return self.question[:80]


class QuizAttempt(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="quiz_attempts")
    quiz = models.ForeignKey(Quiz, on_delete=models.CASCADE, related_name="attempts")
    score = models.PositiveIntegerField(default=0)
    total_questions = models.PositiveIntegerField(default=0)
    answers = models.JSONField(blank=True, null=True)
    completed_at = models.DateTimeField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["owner", "quiz"]),
        ]

    def __str__(self) -> str:
        return f"{self.owner} - {self.quiz} ({self.score}/{self.total_questions})"

