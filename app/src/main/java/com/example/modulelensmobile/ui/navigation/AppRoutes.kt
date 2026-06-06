package com.example.modulelensmobile.ui.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val SUBJECT_DETAIL = "subjectDetail/{subjectId}"
    const val MODULE_READER = "moduleReader/{moduleId}"
    const val SCANS = "scans"
    const val OCR_RESULT = "ocrResult/{scanId}"
    const val AI_SUMMARY = "aiSummary"
    const val PROFILE = "profile"

    fun createSubjectDetailRoute(subjectId: String) = "subjectDetail/$subjectId"
    fun createModuleReaderRoute(moduleId: String) = "moduleReader/$moduleId"
    fun createOcrResultRoute(scanId: String) = "ocrResult/$scanId"
}
