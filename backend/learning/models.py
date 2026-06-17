import uuid

from django.conf import settings
from django.core.validators import MaxValueValidator, MinValueValidator
from django.db import models
from django.utils import timezone


def module_file_upload_path(instance, filename: str) -> str:
    user_id = instance.owner_id or "unknown"
    return f"modules/user_{user_id}/{uuid.uuid4()}_{filename}"


def board_scan_upload_path(instance, filename: str) -> str:
    user_id = instance.owner_id or "unknown"
    return f"board_scans/user_{user_id}/{uuid.uuid4()}_{filename}"


class Subject(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="subjects")
    title = models.CharField(max_length=150)
    description = models.TextField(blank=True)
    color = models.CharField(max_length=30, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["title"]
        constraints = [
            models.UniqueConstraint(fields=["owner", "title"], name="unique_subject_title_per_owner"),
        ]

    def __str__(self) -> str:
        return self.title


class Module(models.Model):
    class ContentType(models.TextChoices):
        MARKDOWN = "markdown", "Markdown"
        TEXT = "text", "Text"
        PDF = "pdf", "PDF"
        DOCX = "docx", "DOCX"
        PPTX = "pptx", "PPTX"

    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="modules")
    subject = models.ForeignKey(Subject, on_delete=models.CASCADE, related_name="modules")
    title = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    content_type = models.CharField(max_length=20, choices=ContentType.choices, default=ContentType.MARKDOWN)
    markdown_content = models.TextField(blank=True)
    # TODO: Future phase: extract readable text from uploaded PDFs for search and AI context.
    # TODO: Future phase: add DOCX/PPTX text extraction and optional conversion to PDF.
    extracted_text = models.TextField(blank=True)
    module_file = models.FileField(upload_to=module_file_upload_path, blank=True, null=True)
    original_filename = models.CharField(max_length=255, blank=True)
    is_favorite = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["title"]
        indexes = [
            models.Index(fields=["owner", "subject"]),
            models.Index(fields=["content_type"]),
        ]

    def __str__(self) -> str:
        return self.title


class Chapter(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="chapters")
    module = models.ForeignKey(Module, on_delete=models.CASCADE, related_name="chapters")
    title = models.CharField(max_length=200)
    order = models.PositiveIntegerField(default=0)
    markdown_content = models.TextField(blank=True)
    extracted_text = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["order", "title"]
        indexes = [
            models.Index(fields=["owner", "module", "order"]),
        ]

    def __str__(self) -> str:
        return self.title


class Tag(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="tags")
    name = models.CharField(max_length=80)
    color = models.CharField(max_length=30, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["name"]
        constraints = [
            models.UniqueConstraint(fields=["owner", "name"], name="unique_tag_name_per_owner"),
        ]

    def __str__(self) -> str:
        return self.name


class BoardScan(models.Model):
    class ReviewStatus(models.TextChoices):
        NEW = "new", "New"
        NEEDS_REVIEW = "needs_review", "Needs review"
        REVIEWED = "reviewed", "Reviewed"
        MASTERED = "mastered", "Mastered"

    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="board_scans")
    subject = models.ForeignKey(Subject, on_delete=models.SET_NULL, related_name="board_scans", blank=True, null=True)
    module = models.ForeignKey(Module, on_delete=models.SET_NULL, related_name="board_scans", blank=True, null=True)
    chapter = models.ForeignKey(Chapter, on_delete=models.SET_NULL, related_name="board_scans", blank=True, null=True)
    image = models.ImageField(upload_to=board_scan_upload_path, blank=True, null=True)
    raw_ocr_text = models.TextField(blank=True)
    cleaned_text = models.TextField(blank=True)
    # TODO: Future phase: AI summaries can store a short board-note summary here or in a Summary model.
    summary = models.TextField(blank=True)
    review_status = models.CharField(
        max_length=20,
        choices=ReviewStatus.choices,
        default=ReviewStatus.NEW,
    )
    tags = models.ManyToManyField(Tag, related_name="board_scans", blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["owner", "review_status"]),
            models.Index(fields=["owner", "subject"]),
            models.Index(fields=["owner", "module"]),
        ]

    def __str__(self) -> str:
        if self.chapter:
            return f"Board scan for {self.chapter.title}"
        if self.module:
            return f"Board scan for {self.module.title}"
        return f"Board scan #{self.pk}"


class ReadingProgress(models.Model):
    class Status(models.TextChoices):
        NOT_STARTED = "not_started", "Not started"
        READING = "reading", "Reading"
        COMPLETED = "completed", "Completed"

    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="reading_progress")
    module = models.ForeignKey(Module, on_delete=models.CASCADE, related_name="reading_progress")
    chapter = models.ForeignKey(Chapter, on_delete=models.CASCADE, related_name="reading_progress", blank=True, null=True)
    progress_percentage = models.PositiveSmallIntegerField(
        default=0,
        validators=[MinValueValidator(0), MaxValueValidator(100)],
    )
    last_position = models.CharField(max_length=255, blank=True)
    last_read_at = models.DateTimeField(auto_now=True)
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.NOT_STARTED)

    class Meta:
        ordering = ["-last_read_at"]
        indexes = [
            models.Index(fields=["owner", "module", "chapter"]),
            models.Index(fields=["owner", "status"]),
        ]
        constraints = [
            models.UniqueConstraint(
                fields=["owner", "module", "chapter"],
                name="unique_reading_progress_per_target",
                nulls_distinct=False,
            ),
        ]

    def __str__(self) -> str:
        return f"{self.owner} - {self.module} ({self.progress_percentage}%)"


class SubjectPost(models.Model):
    class PostType(models.TextChoices):
        ANNOUNCEMENT = "announcement", "Announcement"
        REMINDER = "reminder", "Reminder"
        UPDATE = "update", "Update"

    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="subject_posts")
    subject = models.ForeignKey(Subject, on_delete=models.CASCADE, related_name="posts")
    title = models.CharField(max_length=200)
    content = models.TextField()
    post_type = models.CharField(max_length=20, choices=PostType.choices, default=PostType.ANNOUNCEMENT)
    posted_at = models.DateTimeField(default=timezone.now)
    is_pinned = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["-is_pinned", "-posted_at", "-created_at"]
        indexes = [
            models.Index(fields=["owner", "subject"]),
            models.Index(fields=["owner", "post_type"]),
            models.Index(fields=["owner", "is_pinned"]),
            models.Index(fields=["owner", "posted_at"]),
        ]

    def __str__(self) -> str:
        return self.title
