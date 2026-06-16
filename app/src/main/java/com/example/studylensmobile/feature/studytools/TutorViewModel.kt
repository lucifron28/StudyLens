package com.example.studylensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AiRepository
import com.example.studylensmobile.domain.model.TutorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TutorViewModel(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TutorUiState())
    val uiState: StateFlow<TutorUiState> = _uiState.asStateFlow()

    init {
        startTutor(forceRefresh = false)
    }

    fun startTutor(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStarting = true,
                    isSending = false,
                    errorMessage = null
                )
            }

            val result = aiRepository.startTutor(
                sourceType = sourceType,
                sourceId = sourceId,
                forceRefresh = forceRefresh
            )

            _uiState.update { state ->
                result.fold(
                    onSuccess = { conversation ->
                        state.copy(
                            isStarting = false,
                            session = conversation.session,
                            messages = conversation.messages,
                            draftMessage = "",
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isStarting = false,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun updateDraft(message: String) {
        _uiState.update { it.copy(draftMessage = message) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val sessionId = state.session?.id ?: return
        val content = state.draftMessage.trim()
        if (content.isBlank() || state.isSending) return

        val userMessage = TutorMessage(
            id = "local-${System.currentTimeMillis()}",
            sessionId = sessionId,
            role = "User",
            content = content
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    draftMessage = "",
                    messages = it.messages + userMessage,
                    errorMessage = null
                )
            }

            val result = aiRepository.sendTutorMessage(
                sessionId = sessionId,
                message = content
            )

            _uiState.update { current ->
                result.fold(
                    onSuccess = { turn ->
                        current.copy(
                            isSending = false,
                            session = turn.session,
                            messages = current.messages + turn.message,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSending = false,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }
}
