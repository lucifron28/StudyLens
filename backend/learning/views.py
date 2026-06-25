from django.db.models import Avg, Count, Q
from drf_spectacular.utils import extend_schema
from rest_framework import permissions, status, viewsets
from rest_framework.decorators import action
from rest_framework.filters import OrderingFilter, SearchFilter
from rest_framework.parsers import FormParser, JSONParser, MultiPartParser
from rest_framework.response import Response
from rest_framework.views import APIView

from accounts.permissions import IsOwner
from learning.filters import apply_exact_query_filters
from learning.models import BoardScan, Chapter, Module, ReadingProgress, StudyTask, Subject, Tag
from learning.serializers import (
    BoardScanSerializer,
    ChapterSerializer,
    DashboardSerializer,
    ModuleSerializer,
    ReadingProgressSerializer,
    SubjectSerializer,
    SubjectOverviewSerializer,
    StudyTaskSerializer,
    TagSerializer,
)
from studytools.models import Quiz, QuizAttempt, Summary


def _activity_item(item_type: str, item_id: int, title: str, description: str, created_at):
    return {
        "type": item_type,
        "id": item_id,
        "title": title,
        "description": description,
        "created_at": created_at,
    }


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


class DashboardView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    @extend_schema(responses=DashboardSerializer)
    def get(self, request):
        user = request.user
        progress_qs = ReadingProgress.objects.select_related("module", "chapter").filter(owner=user)
        average_progress = progress_qs.aggregate(value=Avg("progress_percentage"))["value"] or 0

        recent_tasks = StudyTask.objects.select_related("subject").filter(owner=user).order_by(
            "-is_pinned",
            "-posted_at",
        )[:5]

        upcoming = []
        for task in recent_tasks:
            upcoming.append(
                {
                    "type": "task",
                    "id": task.id,
                    "title": task.title,
                    "description": task.content[:100] + ("..." if len(task.content) > 100 else ""),
                    "subject": task.subject.id,
                    "subject_title": task.subject.title,
                    "posted_at": task.created_at,
                }
            )

        continue_learning = [
            {
                "id": progress.id,
                "module": progress.module_id,
                "module_title": progress.module.title,
                "chapter": progress.chapter_id,
                "chapter_title": progress.chapter.title if progress.chapter else "",
                "progress_percentage": progress.progress_percentage,
                "last_position": progress.last_position,
                "status": progress.status,
                "last_read_at": progress.last_read_at,
            }
            for progress in progress_qs.order_by("-last_read_at")[:5]
        ]

        recent_activity = []
        recent_activity.extend(
            _activity_item("board_scan", scan.id, "Board scan saved", scan.cleaned_text[:120], scan.created_at)
            for scan in BoardScan.objects.filter(owner=user).order_by("-created_at")[:3]
        )
        recent_activity.extend(
            _activity_item("task", task.id, task.title, task.content[:120], task.created_at)
            for task in StudyTask.objects.filter(owner=user).order_by("-created_at")[:3]
        )
        recent_activity.extend(
            _activity_item("summary", summary.id, "Summary generated", summary.content[:120], summary.created_at)
            for summary in Summary.objects.filter(owner=user).order_by("-created_at")[:2]
        )
        recent_activity.extend(
            _activity_item("quiz", quiz.id, quiz.title, quiz.description[:120], quiz.created_at)
            for quiz in Quiz.objects.filter(owner=user).order_by("-created_at")[:2]
        )
        recent_activity = sorted(recent_activity, key=lambda item: item["created_at"], reverse=True)[:8]

        data = {
            "overall_progress": round(average_progress),
            "stats": {
                "modules_in_progress": progress_qs.filter(status=ReadingProgress.Status.READING)
                .values("module_id")
                .distinct()
                .count(),
                "notes_saved": BoardScan.objects.filter(owner=user).count(),
                "quizzes_completed": QuizAttempt.objects.filter(owner=user, completed_at__isnull=False).count(),
            },
            "upcoming": upcoming,
            "continue_learning": continue_learning,
            "recent_activity": recent_activity,
        }
        return Response(data)


class SubjectViewSet(OwnedModelViewSet):
    queryset = Subject.objects.all()
    serializer_class = SubjectSerializer
    search_fields = ["title", "description"]
    ordering_fields = ["title", "created_at", "updated_at"]
    ordering = ["title"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return queryset.annotate(
            module_count_value=Count("modules", distinct=True),
            board_scan_count_value=Count("board_scans", distinct=True),
            task_count_value=Count("tasks", distinct=True),
            progress_average=Avg(
                "modules__reading_progress__progress_percentage",
                filter=Q(modules__reading_progress__owner=self.request.user),
            ),
        )

    @extend_schema(responses=SubjectOverviewSerializer)
    @action(detail=True, methods=["get"])
    def overview(self, request, pk=None):
        subject = self.get_object()
        subject_data = SubjectSerializer(subject, context={"request": request}).data
        user = request.user

        latest_modules = Module.objects.filter(owner=user, subject=subject).order_by("-updated_at")[:5]
        recent_board_scans = BoardScan.objects.filter(owner=user, subject=subject).order_by("-created_at")[:5]
        latest_tasks = StudyTask.objects.filter(owner=user, subject=subject).order_by("-is_pinned", "-created_at")[:5]

        return Response(
            {
                "id": subject.id,
                "title": subject.title,
                "description": subject.description,
                "module_count": subject_data["module_count"],
                "board_scan_count": subject_data["board_scan_count"],
                "task_count": subject_data["task_count"],
                "progress_percentage": subject_data["progress_percentage"],
                "latest_modules": [
                    {
                        "id": module.id,
                        "title": module.title,
                        "description": module.description,
                        "content_type": module.content_type,
                        "is_favorite": module.is_favorite,
                        "updated_at": module.updated_at,
                    }
                    for module in latest_modules
                ],
                "recent_board_scans": [
                    {
                        "id": scan.id,
                        "cleaned_text": scan.cleaned_text,
                        "review_status": scan.review_status,
                        "created_at": scan.created_at,
                    }
                    for scan in recent_board_scans
                ],
                "tasks": [
                    {
                        "id": task.id,
                        "title": task.title,
                        "content": task.content,
                        "task_type": task.task_type,
                        "is_completed": task.is_completed,
                        "due_date": task.due_date,
                        "is_pinned": task.is_pinned,
                        "created_at": task.created_at,
                    }
                    for task in latest_tasks
                ],
            }
        )


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

    @extend_schema(request=ReadingProgressSerializer, responses=ReadingProgressSerializer)
    @action(detail=False, methods=["post"], url_path="set")
    def set(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        validated_data = serializer.validated_data.copy()
        module = validated_data.pop("module")
        chapter = validated_data.pop("chapter", None)

        progress, created = ReadingProgress.objects.update_or_create(
            owner=request.user,
            module=module,
            chapter=chapter,
            defaults=validated_data,
        )
        response_status = status.HTTP_201_CREATED if created else status.HTTP_200_OK
        return Response(self.get_serializer(progress).data, status=response_status)


class StudyTaskViewSet(OwnedModelViewSet):
    queryset = StudyTask.objects.select_related("subject").all()
    serializer_class = StudyTaskSerializer
    search_fields = ["title", "content", "subject__title"]
    ordering_fields = ["created_at", "updated_at", "is_pinned", "is_completed", "due_date"]
    ordering = ["-is_pinned", "is_completed", "-created_at"]

    def get_queryset(self):
        queryset = super().get_queryset()
        return apply_exact_query_filters(
            queryset,
            self.request.query_params,
            {
                "subject": "subject_id",
                "post_type": "post_type",
                "is_pinned": "is_pinned",
            },
            integer_params={"subject"},
            boolean_params={"is_pinned"},
        )
