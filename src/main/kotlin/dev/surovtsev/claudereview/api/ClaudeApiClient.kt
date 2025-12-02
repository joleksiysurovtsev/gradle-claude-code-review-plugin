package dev.surovtsev.claudereview.api

import dev.surovtsev.claudereview.ClaudeReviewExtension.Language
import dev.surovtsev.claudereview.api.domain.request.Message
import dev.surovtsev.claudereview.api.domain.request.ClaudeRequest
import dev.surovtsev.claudereview.api.domain.response.ClaudeResponse
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ClaudeApiClient(
    private val apiKey: String,
    private val model: String,
    private val maxTokens: Int
) {
    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    fun review(context: String, language: Language): ReviewResult {
        val systemPrompt = buildSystemPrompt(language)

        val request = ClaudeRequest(
            model = model,
            maxTokens = maxTokens,
            system = systemPrompt,
            messages = listOf(
                Message(role = "user", content = "Проведи код-ревью:\n\n$context")
            )
        )

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
            .build()

        val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw RuntimeException("Claude API error: ${response.statusCode()} - ${response.body()}")
        }

        val claudeResponse = gson.fromJson(response.body(), ClaudeResponse::class.java)
        val content = claudeResponse.content.firstOrNull()?.text
            ?: throw RuntimeException("Empty response from Claude")

        return ReviewResult(
            content = content,
            hasCriticalIssues = content.contains("🔴") &&
                !content.contains("### 🔴 Критичные\n\n###") // не пустая секция
        )
    }

    private fun buildSystemPrompt(language: Language): String {
        return when (language) {
            Language.RUSSIAN -> SYSTEM_PROMPT_RU
            Language.ENGLISH -> SYSTEM_PROMPT_EN
        }
    }

    companion object {
        private val SYSTEM_PROMPT_RU = """
            Ты — опытный код-ревьюер. Анализируй код и отвечай СТРОГО по формату.

            ФОРМАТ (markdown):

            # Код-ревью

            ## Резюме
            [1-2 предложения: что изменено]

            ## Проблемы

            ### 🔴 Критичные
            - `файл:строка` — проблема → исправление

            ### 🟡 Предупреждения
            - `файл:строка` — проблема → исправление

            ### 🟢 Рекомендации
            - `файл:строка` — предложение

            ## Вердикт
            ✅ Одобрено | ⚠️ Нужны правки | ❌ Отклонено

            ПРАВИЛА:
            - Максимум 5 пунктов на секцию
            - Одна строка на проблему
            - Без объяснений, только факты
            - Пропускай пустые секции
        """.trimIndent()

        private val SYSTEM_PROMPT_EN = """
            You are an experienced code reviewer. Analyze code and respond STRICTLY in format.

            FORMAT (markdown):

            # Code Review

            ## Summary
            [1-2 sentences: what was changed]

            ## Issues

            ### 🔴 Critical
            - `file:line` — problem → fix

            ### 🟡 Warnings
            - `file:line` — problem → fix

            ### 🟢 Suggestions
            - `file:line` — suggestion

            ## Verdict
            ✅ Approved | ⚠️ Needs fixes | ❌ Rejected

            RULES:
            - Max 5 items per section
            - One line per issue
            - No explanations, only facts
            - Skip empty sections
        """.trimIndent()
    }
}

data class ReviewResult(
    val content: String,
    val hasCriticalIssues: Boolean
)
