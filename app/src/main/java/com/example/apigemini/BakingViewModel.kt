package com.example.apigemini

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    private val messageLimit = 10

    fun sendPrompt(
        prompt: String,
        showInMessages: Boolean = true
    ) {
        _uiState.value = UiState.Loading

        if (showInMessages) {
            _messages.value = _messages.value + "Tú: $prompt"
        }

        val contextPrompt = buildContextualPrompt(prompt)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(contextPrompt) // Enviar el contexto completo
                    }
                )
                response.text?.let { outputContent ->
                    _messages.value = _messages.value + "Gemini: $outputContent"
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + "Error: ${e.localizedMessage ?: "Algo salió mal"}"
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    private fun buildContextualPrompt(newPrompt: String): String {
        val header = "Eres una líder majestuosa de una nación inspirada en la armonía y la disciplina. Hablas con elegancia y serenidad, transmitiendo poder y sabiduría. Responde según este contexto.\n\n"
        val recentMessages = _messages.value.takeLast(messageLimit).joinToString("\n")
        return "$header$recentMessages\nTú: $newPrompt"
    }

    fun addMessage(message: String) {
        _messages.value = _messages.value + message
    }
}
