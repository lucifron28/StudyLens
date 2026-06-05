from rest_framework.routers import DefaultRouter

from learning.views import BoardScanViewSet, ChapterViewSet, ModuleViewSet, ReadingProgressViewSet, SubjectViewSet, TagViewSet


router = DefaultRouter()
router.register("subjects", SubjectViewSet, basename="subject")
router.register("modules", ModuleViewSet, basename="module")
router.register("chapters", ChapterViewSet, basename="chapter")
router.register("tags", TagViewSet, basename="tag")
router.register("board-scans", BoardScanViewSet, basename="board-scan")
router.register("reading-progress", ReadingProgressViewSet, basename="reading-progress")

urlpatterns = router.urls

