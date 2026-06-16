package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.TutorMessage
import com.example.studylensmobile.domain.model.TutorSession

data class TutorUiState(
    val isStarting: Boolean = true,
    val isSending: Boolean = false,
    val session: TutorSession? = null,
    val messages: List<TutorMessage> = emptyList(),
    val draftMessage: String = "",
    val errorMessage: String? = null
) {
    val canSend: Boolean
        get() = draftMessage.isNotBlank() && !isStarting && !isSending && session?.isMastered != true

    val progressLabel: String
        get() = session?.progressLabel ?: "0 / 3 clear"
}
