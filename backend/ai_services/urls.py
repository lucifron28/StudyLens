from django.urls import path
from rest_framework.routers import DefaultRouter

from ai_services.views import (
    GenerateFlashcardsView,
    GenerateQuizView,
    StartTutorView,
    SummarizeView,
    TutorMessageView,
    TutorMessageViewSet,
    TutorSessionViewSet,
)


router = DefaultRouter()
router.register("tutor-sessions", TutorSessionViewSet, basename="tutor-session")
router.register("tutor-messages", TutorMessageViewSet, basename="tutor-message-record")

urlpatterns = [
    path("summarize/", SummarizeView.as_view(), name="ai-summarize"),
    path("generate-flashcards/", GenerateFlashcardsView.as_view(), name="ai-generate-flashcards"),
    path("generate-quiz/", GenerateQuizView.as_view(), name="ai-generate-quiz"),
    path("start-tutor/", StartTutorView.as_view(), name="ai-start-tutor"),
    path("tutor-message/", TutorMessageView.as_view(), name="ai-tutor-message"),
]

urlpatterns += router.urls

