package dev.surovtsev.claudereview.api.domain.request

import dev.surovtsev.claudereview.api.domain.request.Message
import com.google.gson.annotations.SerializedName

data class ClaudeRequest(
    val model: String,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    val system: String,
    val messages: List<Message>
)
