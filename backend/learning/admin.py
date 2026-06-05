from django.contrib import admin

from learning.models import BoardScan, Chapter, Module, ReadingProgress, Subject, Tag


@admin.register(Subject)
class SubjectAdmin(admin.ModelAdmin):
    list_display = ["title", "owner", "color", "created_at", "updated_at"]
    search_fields = ["title", "description", "owner__username"]
    list_filter = ["created_at", "updated_at"]


@admin.register(Module)
class ModuleAdmin(admin.ModelAdmin):
    list_display = ["title", "subject", "owner", "content_type", "is_favorite", "created_at"]
    search_fields = ["title", "description", "markdown_content", "extracted_text", "owner__username"]
    list_filter = ["content_type", "is_favorite", "created_at", "updated_at"]


@admin.register(Chapter)
class ChapterAdmin(admin.ModelAdmin):
    list_display = ["title", "module", "owner", "order", "created_at"]
    search_fields = ["title", "markdown_content", "extracted_text", "module__title", "owner__username"]
    list_filter = ["created_at", "updated_at"]


@admin.register(Tag)
class TagAdmin(admin.ModelAdmin):
    list_display = ["name", "owner", "color", "created_at"]
    search_fields = ["name", "owner__username"]
    list_filter = ["created_at"]


@admin.register(BoardScan)
class BoardScanAdmin(admin.ModelAdmin):
    list_display = ["id", "owner", "subject", "module", "chapter", "review_status", "created_at"]
    search_fields = ["raw_ocr_text", "cleaned_text", "summary", "owner__username"]
    list_filter = ["review_status", "created_at", "updated_at"]
    filter_horizontal = ["tags"]


@admin.register(ReadingProgress)
class ReadingProgressAdmin(admin.ModelAdmin):
    list_display = ["owner", "module", "chapter", "progress_percentage", "status", "last_read_at"]
    search_fields = ["owner__username", "module__title", "chapter__title", "last_position"]
    list_filter = ["status", "last_read_at"]

