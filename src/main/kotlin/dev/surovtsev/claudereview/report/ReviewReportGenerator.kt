package dev.surovtsev.claudereview.report

import dev.surovtsev.claudereview.ClaudeReviewExtension.OutputFormat
import dev.surovtsev.claudereview.api.ReviewResult
import com.google.gson.GsonBuilder
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReviewReportGenerator(
    private val outputDir: File,
    private val format: OutputFormat
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val timestamp = LocalDateTime.now()

    fun generate(
        reviewResult: ReviewResult,
        targetBranch: String,
        filesCount: Int
    ): File {
        outputDir.mkdirs()

        return when (format) {
            OutputFormat.MARKDOWN -> generateMarkdown(reviewResult, targetBranch, filesCount)
            OutputFormat.HTML -> generateHtml(reviewResult, targetBranch, filesCount)
            OutputFormat.JSON -> generateJson(reviewResult, targetBranch, filesCount)
        }
    }

    private fun generateMarkdown(
        reviewResult: ReviewResult,
        targetBranch: String,
        filesCount: Int
    ): File {
        val fileName = "review-${timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))}.md"
        val file = File(outputDir, fileName)

        val content = """
            |# Code Review Report
            |
            |**Date:** ${timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}
            |**Branch:** $targetBranch
            |**Files reviewed:** $filesCount
            |**Status:** ${if (reviewResult.hasCriticalIssues) "❌ Critical issues found" else "✅ No critical issues"}
            |
            |---
            |
            |${reviewResult.content}
        """.trimMargin()

        file.writeText(content)
        return file
    }

    private fun generateHtml(
        reviewResult: ReviewResult,
        targetBranch: String,
        filesCount: Int
    ): File {
        val fileName = "review-${timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))}.html"
        val file = File(outputDir, fileName)

        val markdownContent = reviewResult.content
            .replace("### 🔴", "<h3 style=\"color: #dc3545;\">🔴")
            .replace("### 🟡", "<h3 style=\"color: #ffc107;\">🟡")
            .replace("### 🟢", "<h3 style=\"color: #28a745;\">🟢")
            .replace("## ", "<h2>")
            .replace("# ", "<h1>")
            .replace("\n- ", "\n<li>")
            .replace("`([^`]+)`".toRegex(), "<code>$1</code>")
            .replace("\n\n", "</p><p>")

        val statusColor = if (reviewResult.hasCriticalIssues) "#dc3545" else "#28a745"
        val statusText = if (reviewResult.hasCriticalIssues) "Critical issues found" else "No critical issues"

        val html = """
            |<!DOCTYPE html>
            |<html lang="en">
            |<head>
            |    <meta charset="UTF-8">
            |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
            |    <title>Code Review Report</title>
            |    <style>
            |        body {
            |            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            |            max-width: 900px;
            |            margin: 0 auto;
            |            padding: 20px;
            |            line-height: 1.6;
            |            color: #333;
            |        }
            |        .header {
            |            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            |            color: white;
            |            padding: 20px;
            |            border-radius: 10px;
            |            margin-bottom: 20px;
            |        }
            |        .meta {
            |            display: grid;
            |            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            |            gap: 10px;
            |            margin-top: 15px;
            |        }
            |        .meta-item {
            |            background: rgba(255,255,255,0.2);
            |            padding: 10px;
            |            border-radius: 5px;
            |        }
            |        .status {
            |            display: inline-block;
            |            padding: 5px 15px;
            |            border-radius: 20px;
            |            background: $statusColor;
            |            color: white;
            |            font-weight: bold;
            |        }
            |        .content {
            |            background: #f8f9fa;
            |            padding: 20px;
            |            border-radius: 10px;
            |        }
            |        code {
            |            background: #e9ecef;
            |            padding: 2px 6px;
            |            border-radius: 3px;
            |            font-family: 'Monaco', 'Menlo', monospace;
            |            font-size: 0.9em;
            |        }
            |        h1, h2, h3 { margin-top: 20px; }
            |        li { margin: 8px 0; }
            |    </style>
            |</head>
            |<body>
            |    <div class="header">
            |        <h1 style="margin: 0;">📋 Code Review Report</h1>
            |        <div class="meta">
            |            <div class="meta-item">📅 ${timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}</div>
            |            <div class="meta-item">🌿 $targetBranch</div>
            |            <div class="meta-item">📁 $filesCount files</div>
            |        </div>
            |        <div style="margin-top: 15px;">
            |            <span class="status">$statusText</span>
            |        </div>
            |    </div>
            |    <div class="content">
            |        $markdownContent
            |    </div>
            |</body>
            |</html>
        """.trimMargin()

        file.writeText(html)
        return file
    }

    private fun generateJson(
        reviewResult: ReviewResult,
        targetBranch: String,
        filesCount: Int
    ): File {
        val fileName = "review-${timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))}.json"
        val file = File(outputDir, fileName)

        val report = JsonReport(
            timestamp = timestamp.toString(),
            targetBranch = targetBranch,
            filesReviewed = filesCount,
            hasCriticalIssues = reviewResult.hasCriticalIssues,
            content = reviewResult.content,
            issues = parseIssues(reviewResult.content)
        )

        file.writeText(gson.toJson(report))
        return file
    }

    private fun parseIssues(content: String): JsonIssues {
        return JsonIssues(
            critical = extractIssues(content, "🔴"),
            warnings = extractIssues(content, "🟡"),
            suggestions = extractIssues(content, "🟢")
        )
    }

    private fun extractIssues(content: String, emoji: String): List<String> {
        val sectionRegex = """### $emoji[^\n]*\n([\s\S]*?)(?=\n##|\n###|$)""".toRegex()
        val match = sectionRegex.find(content) ?: return emptyList()

        return match.groupValues[1]
            .lines()
            .map { it.trim() }
            .filter { it.startsWith("-") }
            .map { it.removePrefix("-").trim() }
    }

    data class JsonReport(
        val timestamp: String,
        val targetBranch: String,
        val filesReviewed: Int,
        val hasCriticalIssues: Boolean,
        val content: String,
        val issues: JsonIssues
    )

    data class JsonIssues(
        val critical: List<String>,
        val warnings: List<String>,
        val suggestions: List<String>
    )
}
