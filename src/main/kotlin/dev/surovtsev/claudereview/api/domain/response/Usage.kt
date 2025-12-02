package dev.surovtsev.claudereview.api.domain.response

import com.google.gson.annotations.SerializedName

data class Usage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
)
