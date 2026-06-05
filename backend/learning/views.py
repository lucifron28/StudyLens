from rest_framework import permissions, viewsets
from rest_framework.filters import OrderingFilter, SearchFilter
from rest_framework.parsers import FormParser, JSONParser, MultiPartParser

from accounts.permissions import IsOwner
from learning.filters import apply_exact_query_filters
from learning.models import AcademicTask, BoardScan, Chapter, Module, ReadingProgress, Subject, Tag
from learning.serializers import (
    AcademicTaskSerializer,
    BoardScanSerializer,
    ChapterSerializer,
    ModuleSerializer,
    ReadingProgressSerializer,
    SubjectSerializer,
    TagSerializer,
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


class SubjectViewSet(OwnedModelViewSet):
    queryset = Subject.objects.all()
    serializer_class = SubjectSerializer
    search_fields = ["title", "description"]
    ordering_fields = ["title", "created_at", "updated_at"]
    ordering = ["title"]


class ModuleViewSet(OwnedModelViewSet):
    queryset = Module.objects.select_related("subject").all()
    serializer_class = ModuleSerializer
    parser_classes = [JSONParser, FormParser, MultiPartParser]
    search_fields = ["title", "description", "markdown_content", "extracted_text", "original_filename"]
    ordering_fields = ["title", "created_at", "updated_at", "content_type", "is_favorite"]
    ordering = ["title"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "subject": "subject_id",
                "content_type": "content_type",
            },
            integer_params={"subject"},
        )


class ChapterViewSet(OwnedModelViewSet):
    queryset = Chapter.objects.select_related("module", "module__subject").all()
    serializer_class = ChapterSerializer
    search_fields = ["title", "markdown_content", "extracted_text"]
    ordering_fields = ["order", "title", "created_at", "updated_at"]
    ordering = ["order", "title"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {"module": "module_id"},
            integer_params={"module"},
        )


class TagViewSet(OwnedModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer
    search_fields = ["name"]
    ordering_fields = ["name", "created_at"]
    ordering = ["name"]


class BoardScanViewSet(OwnedModelViewSet):
    queryset = BoardScan.objects.select_related("subject", "module", "chapter").prefetch_related("tags").all()
    serializer_class = BoardScanSerializer
    parser_classes = [JSONParser, FormParser, MultiPartParser]
    search_fields = ["raw_ocr_text", "cleaned_text", "summary", "tags__name"]
    ordering_fields = ["created_at", "updated_at", "review_status"]
    ordering = ["-created_at"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "subject": "subject_id",
                "module": "module_id",
                "chapter": "chapter_id",
                "review_status": "review_status",
            },
            integer_params={"subject", "module", "chapter"},
        ).distinct()


class ReadingProgressViewSet(OwnedModelViewSet):
    queryset = ReadingProgress.objects.select_related("module", "chapter").all()
    serializer_class = ReadingProgressSerializer
    search_fields = ["module__title", "chapter__title", "last_position"]
    ordering_fields = ["last_read_at", "progress_percentage", "status"]
    ordering = ["-last_read_at"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "module": "module_id",
                "chapter": "chapter_id",
                "status": "status",
            },
            integer_params={"module", "chapter"},
        )


class AcademicTaskViewSet(OwnedModelViewSet):
    queryset = AcademicTask.objects.select_related("subject", "module", "chapter").all()
    serializer_class = AcademicTaskSerializer
    search_fields = ["title", "description", "subject__title", "module__title"]
    ordering_fields = ["due_at", "created_at", "updated_at", "priority", "status"]
    ordering = ["due_at", "-created_at"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "subject": "subject_id",
                "module": "module_id",
                "status": "status",
                "task_type": "task_type",
                "priority": "priority",
            },
            integer_params={"subject", "module"},
        )
