from rest_framework.routers import DefaultRouter

from studytools.views import FlashcardViewSet, QuizAttemptViewSet, QuizQuestionViewSet, QuizViewSet, SummaryViewSet


router = DefaultRouter()
router.register("summaries", SummaryViewSet, basename="summary")
router.register("flashcards", FlashcardViewSet, basename="flashcard")
router.register("quizzes", QuizViewSet, basename="quiz")
router.register("quiz-questions", QuizQuestionViewSet, basename="quiz-question")
router.register("quiz-attempts", QuizAttemptViewSet, basename="quiz-attempt")

urlpatterns = router.urls

