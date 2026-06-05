from django.db.models import Avg
from drf_spectacular.utils import extend_schema
from rest_framework import permissions, viewsets
from rest_framework.filters import OrderingFilter, SearchFilter
from rest_framework.parsers import FormParser, JSONParser, MultiPartParser
from rest_framework.response import Response
from rest_framework.views import APIView

from accounts.permissions import IsOwner
from learning.filters import apply_exact_query_filters
from learning.models import AcademicTask, BoardScan, Chapter, Module, ReadingProgress, Subject, SubjectPost, Tag
from learning.serializers import (
    AcademicTaskSerializer,
    BoardScanSerializer,
    ChapterSerializer,
    DashboardSerializer,
    ModuleSerializer,
    ReadingProgressSerializer,
    SubjectSerializer,
    SubjectPostSerializer,
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

        upcoming_tasks = AcademicTask.objects.select_related("subject", "module").filter(
            owner=user,
            status__in=[AcademicTask.Status.PENDING, AcademicTask.Status.IN_PROGRESS, AcademicTask.Status.OVERDUE],
        ).order_by("due_at", "-created_at")[:5]
        recent_posts = SubjectPost.objects.select_related("subject").filter(owner=user).order_by(
            "-is_pinned",
            "-posted_at",
        )[:3]

        upcoming = [
            {
                "type": "task",
                "id": task.id,
                "title": task.title,
                "description": task.description,
                "subject": task.subject_id,
                "subject_title": task.subject.title,
                "module": task.module_id,
                "module_title": task.module.title if task.module else "",
                "status": task.status,
                "priority": task.priority,
                "due_at": task.due_at,
            }
            for task in upcoming_tasks
        ]
        upcoming.extend(
            {
                "type": "post",
                "id": post.id,
                "title": post.title,
                "description": post.content[:160],
                "subject": post.subject_id,
                "subject_title": post.subject.title,
                "posted_at": post.posted_at,
            }
            for post in recent_posts
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
            _activity_item("task", task.id, task.title, task.status, task.updated_at)
            for task in AcademicTask.objects.filter(owner=user).order_by("-updated_at")[:3]
        )
        recent_activity.extend(
            _activity_item("post", post.id, post.title, post.content[:120], post.created_at)
            for post in SubjectPost.objects.filter(owner=user).order_by("-created_at")[:3]
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
                "pending_tasks": AcademicTask.objects.filter(
                    owner=user,
                    status__in=[AcademicTask.Status.PENDING, AcademicTask.Status.IN_PROGRESS],
                ).count(),
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


class SubjectPostViewSet(OwnedModelViewSet):
    queryset = SubjectPost.objects.select_related("subject").all()
    serializer_class = SubjectPostSerializer
    search_fields = ["title", "content", "subject__title"]
    ordering_fields = ["posted_at", "created_at", "updated_at", "is_pinned"]
    ordering = ["-is_pinned", "-posted_at", "-created_at"]

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
        )
