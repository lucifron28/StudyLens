from django.contrib import admin

from ai_services.models import TutorMessage, TutorSession


class TutorMessageInline(admin.TabularInline):
    model = TutorMessage
    extra = 0


@admin.register(TutorSession)
class TutorSessionAdmin(admin.ModelAdmin):
    list_display = ["title", "owner", "status", "clear_answers_count", "target_clear_answers", "updated_at"]
    search_fields = ["title", "owner__username", "module__title", "chapter__title"]
    list_filter = ["status", "created_at", "updated_at"]
    inlines = [TutorMessageInline]


@admin.register(TutorMessage)
class TutorMessageAdmin(admin.ModelAdmin):
    list_display = ["id", "session", "role", "clarity_result", "created_at"]
    search_fields = ["content", "session__title", "session__owner__username"]
    list_filter = ["role", "clarity_result", "created_at"]

