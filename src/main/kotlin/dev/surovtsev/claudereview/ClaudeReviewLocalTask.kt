package dev.surovtsev.claudereview

import dev.surovtsev.claudereview.api.ReviewResult
import dev.surovtsev.claudereview.git.GitDiffProvider
import dev.surovtsev.claudereview.report.ReviewReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

abstract class ClaudeReviewLocalTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val claudePath: Property<String>

    @get:Input
    abstract val targetBranch: Property<String>

    @get:Input
    abstract val fallbackCommits: Property<Int>

    @get:Input
    abstract val includePatterns: ListProperty<String>

    @get:Input
    abstract val excludePatterns: ListProperty<String>

    @get:Input
    abstract val outputDir: Property<String>

    @get:Input
    abstract val outputFormat: Property<ClaudeReviewExtension.OutputFormat>

    @get:Input
    abstract val language: Property<ClaudeReviewExtension.Language>

    @get:Input
    abstract val failOnCritical: Property<Boolean>

    @get:Input
    abstract val maxFileSize: Property<Long>

    @get:OutputDirectory
    val reviewOutputDir: File
        get() = project.file(outputDir.get())

    @TaskAction
    fun review() {
        val claudeBinary = findClaudeBinary()

        logger.lifecycle("Using Claude CLI: $claudeBinary")

        val gitProvider = GitDiffProvider(
            projectDir = project.projectDir,
            targetBranch = targetBranch.get(),
            fallbackCommits = fallbackCommits.get()
        )

        val changedFiles = gitProvider.getChangedFiles()
            .filter { matchesPatterns(it) }

        if (changedFiles.isEmpty()) {
            logger.lifecycle("No files to review")
            return
        }

        logger.lifecycle("Files to review: ${changedFiles.size}")
        changedFiles.forEach { logger.lifecycle("  - $it") }

        val context = gitProvider.buildContext(changedFiles, maxFileSize.get())

        // Save context to temporary file
        val contextFile = File.createTempFile("review-context-", ".txt")
        contextFile.deleteOnExit()
        contextFile.writeText(context)

        // Calling local Claude CLI
        val reviewContent = executeClaudeLocal(claudeBinary, contextFile, language.get())

        val reviewResult = ReviewResult(
            content = reviewContent,
            hasCriticalIssues = reviewContent.contains("🔴") &&
                !reviewContent.contains("### 🔴 critical\n\n###")
        )

        val reportGenerator = ReviewReportGenerator(
            outputDir = reviewOutputDir,
            format = outputFormat.get()
        )

        val reportFile = reportGenerator.generate(
            reviewResult = reviewResult,
            targetBranch = gitProvider.resolvedBranch,
            filesCount = changedFiles.size
        )

        logger.lifecycle("=" .repeat(50))
        logger.lifecycle("Review saved to: ${reportFile.absolutePath}")
        logger.lifecycle("=".repeat(50))
        logger.lifecycle(reviewResult.content)

        if (failOnCritical.get() && reviewResult.hasCriticalIssues) {
            throw GradleException("Code review found critical issues!")
        }

        // Delete temporary file
        contextFile.delete()
    }

    private fun findClaudeBinary(): String {
        // If the path is explicitly specified in the configuration
        if (claudePath.isPresent && claudePath.get().isNotBlank()) {
            val path = claudePath.get()
            if (Files.exists(Paths.get(path))) {
                return path
            }
            throw GradleException("Claude CLI not found at specified path: $path")
        }

        // List of possible search paths
        val possiblePaths = listOfNotNull(
            // npm/nvm path
            "${System.getProperty("user.home")}/.nvm/versions/node/v25.1.0/bin/claude",
            "${System.getProperty("user.home")}/.nvm/versions/node/v24.0.0/bin/claude",
            "${System.getProperty("user.home")}/.nvm/versions/node/v23.0.0/bin/claude",
            "${System.getProperty("user.home")}/.nvm/versions/node/v22.0.0/bin/claude",
            "${System.getProperty("user.home")}/.nvm/versions/node/v20.0.0/bin/claude",
            // Global installation npm
            "/usr/local/bin/claude",
            "/opt/homebrew/bin/claude",
            // Windows paths
            "${System.getenv("APPDATA")}\\npm\\claude.cmd",
            "${System.getenv("APPDATA")}\\npm\\claude",
            // Check through which/where
            findClaudeViaPath()
        )

        // Looking for the first existing path
        for (path in possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                return path
            }
        }

        // If you have not found anything, try to find it through PATH
        val pathClaude = findClaudeViaPath()
        if (pathClaude != null) {
            return pathClaude
        }

        throw GradleException("""
            Claude CLI not found! Please install it first:

            npm install -g @anthropic-ai/claude-cli

            Or specify the path explicitly in build.gradle:
            claudeReviewLocal {
                claudePath = "/path/to/claude"
            }
        """.trimIndent())
    }

    private fun findClaudeViaPath(): String? {
        return try {
            val os = System.getProperty("os.name").lowercase()
            val command = if (os.contains("win")) "where" else "which"

            val output = ByteArrayOutputStream()
            val process = ProcessBuilder(command, "claude")
                .redirectErrorStream(true)
                .start()

            process.inputStream.copyTo(output)
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val path = output.toString().trim().lines().first()
                if (path.isNotBlank()) path else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun executeClaudeLocal(claudeBinary: String, contextFile: File, language: ClaudeReviewExtension.Language): String {
        val prompt = when (language) {
            ClaudeReviewExtension.Language.RUSSIAN -> PROMPT_RU
            ClaudeReviewExtension.Language.ENGLISH -> PROMPT_EN
        }

        val fullPrompt = """
            Review code in @${contextFile.absolutePath}

            $prompt
        """.trimIndent()

        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()

        val process = ProcessBuilder(
            claudeBinary,
            "-p", fullPrompt,
            "--output-format", "text"
        )
            .directory(project.projectDir)
            .redirectErrorStream(false)
            .start()

        process.inputStream.copyTo(output)
        process.errorStream.copyTo(error)
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            val errorMsg = error.toString()
            throw GradleException("Claude CLI failed with exit code $exitCode:\n$errorMsg")
        }

        return output.toString().trim()
    }

    private fun matchesPatterns(file: String): Boolean {
        val included = includePatterns.get().isEmpty() ||
            includePatterns.get().any { pattern -> matchGlob(pattern, file) }
        val excluded = excludePatterns.get().any { pattern -> matchGlob(pattern, file) }
        return included && !excluded
    }

    private fun matchGlob(pattern: String, path: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("**", "§§§")
            .replace("*", "[^/]*")
            .replace("§§§", ".*")
        return Regex(regex).containsMatchIn(path)
    }

    companion object {
        private val PROMPT_RU = """
            STRICT FORMAT (markdown):

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
            - Отвечай на русском языке
        """.trimIndent()

        private val PROMPT_EN = """
            STRICT FORMAT (markdown):

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
