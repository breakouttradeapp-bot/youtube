package com.aitube.seogenerator.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ── Cerebras API Request ──────────────────────────────────

data class CerebrasRequest(
    val model: String = "llama-3.3-70b",
    val stream: Boolean = false,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val seed: Int = 0,
    @SerializedName("top_p") val topP: Double = 1.0
)

data class ChatMessage(
    val role: String,
    val content: String
)

// ── Cerebras API Response ─────────────────────────────────

data class CerebrasResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val message: ChatMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    @SerializedName("completion_tokens") val completionTokens: Int?,
    @SerializedName("total_tokens") val totalTokens: Int?
)

// ── App Models — @Parcelize for safe Intent passing ───────

@Parcelize
data class SeoContent(
    val title: String = "",
    val description: String = "",
    val tags: String = "",
    val hashtags: String = ""
) : Parcelable

@Parcelize
data class ShortsTitles(
    val titles: List<String> = emptyList()
) : Parcelable

// ── History (stored in SharedPrefs as JSON) ───────────────

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val type: String,
    val topic: String,
    val resultJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ── UI State ──────────────────────────────────────────────

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
