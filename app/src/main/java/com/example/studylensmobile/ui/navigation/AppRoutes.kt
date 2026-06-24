package com.example.studylensmobile.ui.navigation

import android.net.Uri

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val SUBJECT_DETAIL = "subjectDetail/{subjectId}"
    const val MODULE_READER = "moduleReader/{moduleId}"
    const val SCANS = "scans"
    const val OCR_RESULT = "ocrResult/{scanId}"
    const val AI_SUMMARY = "aiSummary/{sourceType}/{sourceId}"
    const val FLASHCARDS = "flashcards/{sourceType}/{sourceId}"
    const val QUIZ = "quiz/{sourceType}/{sourceId}"
    const val TUTOR = "tutor/{sourceType}/{sourceId}"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "editProfile"
    const val CAMERA_CAPTURE = "camera_capture"
    const val IMAGE_CROP = "image_crop/{imageUri}"
    const val PDF_VIEWER = "pdf_viewer/{title}?url={url}"

    fun createSubjectDetailRoute(subjectId: String) = "subjectDetail/$subjectId"
    fun createModuleReaderRoute(moduleId: String) = "moduleReader/$moduleId"
    fun createOcrResultRoute(scanId: String) = "ocrResult/$scanId"
    fun createAiSummaryRoute(sourceType: String, sourceId: String) = "aiSummary/$sourceType/$sourceId"
    fun createFlashcardsRoute(sourceType: String, sourceId: String) = "flashcards/$sourceType/$sourceId"
    fun createQuizRoute(sourceType: String, sourceId: String) = "quiz/$sourceType/$sourceId"
    fun createTutorRoute(sourceType: String, sourceId: String) = "tutor/$sourceType/$sourceId"
    fun createImageCropRoute(imageUri: String) = "image_crop/${Uri.encode(imageUri)}"
    fun createPdfViewerRoute(title: String, url: String) = "pdf_viewer/${Uri.encode(title)}?url=${Uri.encode(url)}"
}
