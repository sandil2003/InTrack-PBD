package com.example.intrack.data.repository

import com.example.intrack.data.ai.FinancialContextBuilder
import com.example.intrack.data.ai.FinancialContextSnapshot
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

interface AiCoachRepository {
    fun sendMessageStream(
        userMessage: String,
        contextSnapshot: FinancialContextSnapshot
    ): Flow<String>

    fun resetSession()
}

@Singleton
class AiCoachRepositoryImpl @Inject constructor() : AiCoachRepository {

    private var activeChat: Chat? = null
    private var lastContextKey: String = ""

    override fun sendMessageStream(
        userMessage: String,
        contextSnapshot: FinancialContextSnapshot
    ): Flow<String> = flow {
        val chat = getOrCreateChat(contextSnapshot)
        chat.sendMessageStream(userMessage).collect { chunk ->
            val piece = chunk.text
            if (!piece.isNullOrEmpty()) {
                emit(piece)
            }
        }
    }.catch { throwable ->
        throw IllegalStateException(mapFriendlyError(throwable), throwable)
    }

    override fun resetSession() {
        activeChat = null
        lastContextKey = ""
    }

    private fun getOrCreateChat(snapshot: FinancialContextSnapshot): Chat {
        val systemText = FinancialContextBuilder.buildSystemInstruction(snapshot)
        val existingChat = activeChat
        if (existingChat != null && lastContextKey == systemText) {
            return existingChat
        }

        val previousHistory = existingChat?.history?.toList().orEmpty()
        val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.6f
                topP = 0.9f
                maxOutputTokens = 1024
            },
            systemInstruction = content(role = "system") {
                text(systemText)
            }
        )
        val newChat = generativeModel.startChat(history = previousHistory)
        activeChat = newChat
        lastContextKey = systemText
        return newChat
    }

    private fun mapFriendlyError(throwable: Throwable): String {
        val message = throwable.message.orEmpty()
        return when {
            message.contains("App Check", ignoreCase = true) ||
                message.contains("attestation", ignoreCase = true) ->
                "App Check verification failed. Make sure your debug token is registered in Firebase Console."
            message.contains("network", ignoreCase = true) ||
                message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ->
                "Unable to reach Gemini right now. Check your internet connection and try again."
            message.contains("API", ignoreCase = true) && message.contains("not enabled", ignoreCase = true) ->
                "Firebase AI Logic API is not enabled for this Firebase project."
            message.contains("quota", ignoreCase = true) ||
                message.contains("429", ignoreCase = true) ->
                "AI rate limit reached. Please wait a moment and try again."
            message.isNotBlank() -> message
            else -> "Something went wrong while generating a response."
        }
    }

    companion object {
        private const val MODEL_NAME = "gemini-3.8-flash"
    }
}
