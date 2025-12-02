package dev.surovtsev.claudereview.api.domain.response

import com.google.gson.annotations.SerializedName

data class ClaudeResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ContentBlock>,
    val model: String,
    @SerializedName("stop_reason")
    val stopReason: String?,
    val usage: Usage?
)

