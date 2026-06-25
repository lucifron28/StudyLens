from django.urls import path
from rest_framework.routers import DefaultRouter

from learning.views import (
    BoardScanViewSet,
    ChapterViewSet,
    DashboardView,
    ModuleViewSet,
    ReadingProgressViewSet,
    SubjectViewSet,
    StudyTaskViewSet,
    TagViewSet,
)


router = DefaultRouter()
router.register("subjects", SubjectViewSet, basename="subject")
router.register("modules", ModuleViewSet, basename="module")
router.register("chapters", ChapterViewSet, basename="chapter")
router.register("tags", TagViewSet, basename="tag")
router.register("board-scans", BoardScanViewSet, basename="board-scan")
router.register("reading-progress", ReadingProgressViewSet, basename="reading-progress")
router.register("tasks", StudyTaskViewSet, basename="task")

urlpatterns = [
    path("dashboard/", DashboardView.as_view(), name="learning-dashboard"),
]

urlpatterns += router.urls
