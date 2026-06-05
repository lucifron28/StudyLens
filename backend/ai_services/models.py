from django.conf import settings
from django.db import models

from learning.models import BoardScan, Chapter, Module


class TutorSession(models.Model):
    class Status(models.TextChoices):
        IN_PROGRESS = "in_progress", "In progress"
        MASTERED = "mastered", "Mastered"
        NEEDS_REVIEW = "needs_review", "Needs review"
        ABANDONED = "abandoned", "Abandoned"

    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="tutor_sessions")
    module = models.ForeignKey(Module, on_delete=models.SET_NULL, related_name="tutor_sessions", blank=True, null=True)
    chapter = models.ForeignKey(Chapter, on_delete=models.SET_NULL, related_name="tutor_sessions", blank=True, null=True)
    board_scan = models.ForeignKey(BoardScan, on_delete=models.SET_NULL, related_name="tutor_sessions", blank=True, null=True)
    title = models.CharField(max_length=200)
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.IN_PROGRESS)
    clear_answers_count = models.PositiveSmallIntegerField(default=0)
    target_clear_answers = models.PositiveSmallIntegerField(default=3)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-updated_at"]
        indexes = [
            models.Index(fields=["owner", "status"]),
            models.Index(fields=["owner", "module"]),
        ]

    def __str__(self) -> str:
        return self.title


class TutorMessage(models.Model):
    class Role(models.TextChoices):
        SYSTEM = "system", "System"
        ASSISTANT = "assistant", "Assistant"
        USER = "user", "User"

    class ClarityResult(models.TextChoices):
        CLEAR = "clear", "Clear"
        UNCLEAR = "unclear", "Unclear"
        PARTIAL = "partial", "Partial"

    session = models.ForeignKey(TutorSession, on_delete=models.CASCADE, related_name="messages")
    role = models.CharField(max_length=20, choices=Role.choices)
    content = models.TextField()
    clarity_result = models.CharField(max_length=20, choices=ClarityResult.choices, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["created_at", "id"]
        indexes = [
            models.Index(fields=["session", "created_at"]),
        ]

    def __str__(self) -> str:
        return f"{self.role}: {self.content[:60]}"

