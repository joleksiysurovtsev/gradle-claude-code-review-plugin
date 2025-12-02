package dev.surovtsev.claudereview

import dev.surovtsev.claudereview.api.ClaudeApiClient
import dev.surovtsev.claudereview.git.GitDiffProvider
import dev.surovtsev.claudereview.report.ReviewReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ClaudeReviewTask : DefaultTask() {

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val model: Property<String>

    @get:Input
    abstract val maxTokens: Property<Int>

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
        validateApiKey()

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

        val client = ClaudeApiClient(
            apiKey = apiKey.get(),
            model = model.get(),
            maxTokens = maxTokens.get()
        )

        logger.lifecycle("Calling Claude API (${model.get()})...")

        val reviewResult = client.review(context, language.get())

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
    }

    private fun validateApiKey() {
        if (!apiKey.isPresent || apiKey.get().isBlank()) {
            throw GradleException(
                "Claude API key not configured. Set ANTHROPIC_API_KEY env or configure in build.gradle"
            )
        }
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
}