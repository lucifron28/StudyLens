from rest_framework import permissions, viewsets
from rest_framework.filters import OrderingFilter, SearchFilter

from accounts.permissions import IsOwner
from learning.filters import apply_exact_query_filters
from studytools.models import Flashcard, Quiz, QuizAttempt, QuizQuestion, Summary
from studytools.serializers import (
    FlashcardSerializer,
    QuizAttemptSerializer,
    QuizQuestionSerializer,
    QuizSerializer,
    SummarySerializer,
)


class OwnedModelViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated, IsOwner]
    filter_backends = [SearchFilter, OrderingFilter]
    ordering = ["-created_at"]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return self.queryset.none()
        return self.queryset.filter(owner=user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)


class SummaryViewSet(OwnedModelViewSet):
    queryset = Summary.objects.select_related("module", "chapter", "board_scan").all()
    serializer_class = SummarySerializer
    search_fields = ["content", "module__title", "chapter__title", "board_scan__cleaned_text"]
    ordering_fields = ["created_at", "source_type", "is_ai_generated"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "module": "module_id",
                "chapter": "chapter_id",
                "board_scan": "board_scan_id",
                "source_type": "source_type",
            },
            integer_params={"module", "chapter", "board_scan"},
        )


class FlashcardViewSet(OwnedModelViewSet):
    queryset = Flashcard.objects.select_related("module", "chapter", "board_scan").all()
    serializer_class = FlashcardSerializer
    search_fields = ["question", "answer", "module__title", "chapter__title", "board_scan__cleaned_text"]
    ordering_fields = ["created_at", "updated_at", "difficulty", "source_type", "is_ai_generated"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "module": "module_id",
                "chapter": "chapter_id",
                "board_scan": "board_scan_id",
                "source_type": "source_type",
                "difficulty": "difficulty",
            },
            integer_params={"module", "chapter", "board_scan"},
        )


class QuizViewSet(OwnedModelViewSet):
    queryset = Quiz.objects.select_related("module", "chapter", "board_scan").prefetch_related("questions").all()
    serializer_class = QuizSerializer
    search_fields = ["title", "description", "module__title", "chapter__title"]
    ordering_fields = ["created_at", "updated_at", "source_type", "is_ai_generated", "title"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "module": "module_id",
                "chapter": "chapter_id",
                "board_scan": "board_scan_id",
                "source_type": "source_type",
            },
            integer_params={"module", "chapter", "board_scan"},
        )


class QuizQuestionViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated]
    queryset = QuizQuestion.objects.select_related("quiz", "quiz__owner").all()
    serializer_class = QuizQuestionSerializer
    filter_backends = [SearchFilter, OrderingFilter]
    search_fields = ["question", "correct_answer", "explanation", "quiz__title"]
    ordering_fields = ["order", "id", "question_type"]
    ordering = ["order", "id"]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return self.queryset.none()
        queryset = self.queryset.filter(quiz__owner=user)
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {"quiz": "quiz_id", "question_type": "question_type"},
            integer_params={"quiz"},
        )


class QuizAttemptViewSet(OwnedModelViewSet):
    queryset = QuizAttempt.objects.select_related("quiz").all()
    serializer_class = QuizAttemptSerializer
    search_fields = ["quiz__title"]
    ordering_fields = ["created_at", "completed_at", "score", "total_questions"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {"quiz": "quiz_id"},
            integer_params={"quiz"},
        )

