package dev.surovtsev.claudereview

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ClaudeReviewExtension {

    // API settings
    abstract val apiKey: Property<String>
    abstract val model: Property<String>
    abstract val maxTokens: Property<Int>

    // Git settings
    abstract val targetBranch: Property<String>
    abstract val fallbackCommits: Property<Int>

    // File filters
    abstract val includePatterns: ListProperty<String>
    abstract val excludePatterns: ListProperty<String>

    // report
    abstract val outputDir: Property<String>
    abstract val outputFormat: Property<OutputFormat>
    abstract val language: Property<Language>

    // behavior
    abstract val failOnCritical: Property<Boolean>
    abstract val maxFileSize: Property<Long>

    // local CLI
    abstract val claudePath: Property<String>

    init {
        model.convention("claude-sonnet-4-20250514")
        maxTokens.convention(2048)
        targetBranch.convention("auto") // auto-detect
        fallbackCommits.convention(5)
        includePatterns.convention(listOf("**/*.kt", "**/*.java", "**/*.ts", "**/*.js"))
        excludePatterns.convention(listOf("**/build/**", "**/node_modules/**", "**/*.generated.*"))
        outputDir.convention("build/reviews")
        outputFormat.convention(OutputFormat.MARKDOWN)
        language.convention(Language.RUSSIAN)
        failOnCritical.convention(false)
        maxFileSize.convention(50_000L)
        claudePath.convention("")  //empty string = autosearch
    }

    enum class OutputFormat { MARKDOWN, HTML, JSON }
    enum class Language { RUSSIAN, ENGLISH }
}