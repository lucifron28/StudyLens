from drf_spectacular.utils import OpenApiResponse, extend_schema
from rest_framework import permissions, status, viewsets
from rest_framework.filters import OrderingFilter, SearchFilter
from rest_framework.response import Response
from rest_framework.views import APIView

from accounts.permissions import IsOwner
from ai_services.models import TutorMessage, TutorSession
from ai_services.providers.base import AIProviderError
from ai_services.serializers import (
    GenerateFlashcardsRequestSerializer,
    GenerateQuizRequestSerializer,
    StartTutorRequestSerializer,
    SummarizeRequestSerializer,
    TutorMessageRequestSerializer,
    TutorMessageResponseSerializer,
    TutorMessageSerializer,
    TutorSessionSerializer,
    TutorStartResponseSerializer,
)
from ai_services.service import AIOutputError, create_flashcards, create_quiz, create_summary, create_tutor_reply, start_tutor_session
from learning.filters import apply_exact_query_filters
from studytools.serializers import FlashcardSerializer, QuizSerializer, SummarySerializer


def ai_error_response(exc: Exception) -> Response:
    return Response({"detail": str(exc)}, status=status.HTTP_502_BAD_GATEWAY)


class OwnedModelViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated, IsOwner]
    filter_backends = [SearchFilter, OrderingFilter]
    ordering = ["-updated_at"]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return self.queryset.none()
        return self.queryset.filter(owner=user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)


class TutorSessionViewSet(OwnedModelViewSet):
    queryset = TutorSession.objects.select_related("module", "chapter", "board_scan").all()
    serializer_class = TutorSessionSerializer
    search_fields = ["title", "module__title", "chapter__title", "board_scan__cleaned_text"]
    ordering_fields = ["created_at", "updated_at", "status", "clear_answers_count"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "module": "module_id",
                "chapter": "chapter_id",
                "board_scan": "board_scan_id",
                "status": "status",
            },
            integer_params={"module", "chapter", "board_scan"},
        )


class TutorMessageViewSet(viewsets.ModelViewSet):
    permission_classes = [permissions.IsAuthenticated]
    queryset = TutorMessage.objects.select_related("session", "session__owner").all()
    serializer_class = TutorMessageSerializer
    filter_backends = [SearchFilter, OrderingFilter]
    search_fields = ["content", "session__title"]
    ordering_fields = ["created_at", "role", "clarity_result"]
    ordering = ["created_at", "id"]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return self.queryset.none()
        queryset = self.queryset.filter(session__owner=user)
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "session": "session_id",
                "role": "role",
                "clarity_result": "clarity_result",
            },
            integer_params={"session"},
        )


class SummarizeView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
        request=SummarizeRequestSerializer,
        responses={201: SummarySerializer, 502: OpenApiResponse(description="AI provider or JSON output error")},
    )
    def post(self, request):
        serializer = SummarizeRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            summary = create_summary(request.user, serializer.validated_data)
        except (AIProviderError, AIOutputError) as exc:
            return ai_error_response(exc)
        return Response(SummarySerializer(summary, context={"request": request}).data, status=status.HTTP_201_CREATED)


class GenerateFlashcardsView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
        request=GenerateFlashcardsRequestSerializer,
        responses={201: FlashcardSerializer(many=True), 502: OpenApiResponse(description="AI provider or JSON output error")},
    )
    def post(self, request):
        serializer = GenerateFlashcardsRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            flashcards = create_flashcards(request.user, serializer.validated_data)
        except (AIProviderError, AIOutputError) as exc:
            return ai_error_response(exc)
        return Response(FlashcardSerializer(flashcards, many=True, context={"request": request}).data, status=status.HTTP_201_CREATED)


class GenerateQuizView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
        request=GenerateQuizRequestSerializer,
        responses={201: QuizSerializer, 502: OpenApiResponse(description="AI provider or JSON output error")},
    )
    def post(self, request):
        serializer = GenerateQuizRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            quiz = create_quiz(request.user, serializer.validated_data)
        except (AIProviderError, AIOutputError) as exc:
            return ai_error_response(exc)
        quiz.refresh_from_db()
        return Response(QuizSerializer(quiz, context={"request": request}).data, status=status.HTTP_201_CREATED)


class StartTutorView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
        request=StartTutorRequestSerializer,
        responses={201: TutorStartResponseSerializer, 502: OpenApiResponse(description="AI provider or JSON output error")},
    )
    def post(self, request):
        serializer = StartTutorRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            session, message = start_tutor_session(request.user, serializer.validated_data)
        except (AIProviderError, AIOutputError) as exc:
            return ai_error_response(exc)
        return Response(
            {
                "session": TutorSessionSerializer(session, context={"request": request}).data,
                "message": TutorMessageSerializer(message, context={"request": request}).data,
            },
            status=status.HTTP_201_CREATED,
        )


class TutorMessageView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(
        request=TutorMessageRequestSerializer,
        responses={201: TutorMessageResponseSerializer, 502: OpenApiResponse(description="AI provider or JSON output error")},
    )
    def post(self, request):
        serializer = TutorMessageRequestSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            session, message = create_tutor_reply(request.user, serializer.validated_data)
        except (AIProviderError, AIOutputError) as exc:
            return ai_error_response(exc)
        return Response(
            {
                "session": TutorSessionSerializer(session, context={"request": request}).data,
                "message": TutorMessageSerializer(message, context={"request": request}).data,
            },
            status=status.HTTP_201_CREATED,
        )

