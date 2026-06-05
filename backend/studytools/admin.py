from django.contrib import admin

from studytools.models import Flashcard, Quiz, QuizAttempt, QuizQuestion, Summary


@admin.register(Summary)
class SummaryAdmin(admin.ModelAdmin):
    list_display = ["id", "owner", "source_type", "is_ai_generated", "created_at"]
    search_fields = ["content", "owner__username", "module__title", "chapter__title"]
    list_filter = ["source_type", "is_ai_generated", "created_at"]


@admin.register(Flashcard)
class FlashcardAdmin(admin.ModelAdmin):
    list_display = ["id", "owner", "difficulty", "source_type", "is_ai_generated", "created_at"]
    search_fields = ["question", "answer", "owner__username", "module__title", "chapter__title"]
    list_filter = ["difficulty", "source_type", "is_ai_generated", "created_at"]


class QuizQuestionInline(admin.TabularInline):
    model = QuizQuestion
    extra = 0


@admin.register(Quiz)
class QuizAdmin(admin.ModelAdmin):
    list_display = ["title", "owner", "source_type", "is_ai_generated", "created_at"]
    search_fields = ["title", "description", "owner__username", "module__title", "chapter__title"]
    list_filter = ["source_type", "is_ai_generated", "created_at"]
    inlines = [QuizQuestionInline]


@admin.register(QuizQuestion)
class QuizQuestionAdmin(admin.ModelAdmin):
    list_display = ["id", "quiz", "question_type", "order"]
    search_fields = ["question", "correct_answer", "explanation", "quiz__title"]
    list_filter = ["question_type"]


@admin.register(QuizAttempt)
class QuizAttemptAdmin(admin.ModelAdmin):
    list_display = ["id", "owner", "quiz", "score", "total_questions", "completed_at", "created_at"]
    search_fields = ["owner__username", "quiz__title"]
    list_filter = ["completed_at", "created_at"]

