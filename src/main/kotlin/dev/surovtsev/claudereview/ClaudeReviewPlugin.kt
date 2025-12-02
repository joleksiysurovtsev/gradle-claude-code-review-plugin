package dev.surovtsev.claudereview

import org.gradle.api.Plugin
import org.gradle.api.Project

class ClaudeReviewPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Register extension
        val extension = project.extensions.create(
            "claudeReview",
            ClaudeReviewExtension::class.java
        )

        // Register Dash for API
        val apiTask = project.tasks.register("claudeReview", ClaudeReviewTask::class.java)
        apiTask.configure {
            group = "verification"
            description = "Run AI-powered code review using Claude API"

            // Skip configuration
            apiKey.set(extension.apiKey.orElse(
                project.providers.environmentVariable("ANTHROPIC_API_KEY")
            ))
            model.set(extension.model)
            maxTokens.set(extension.maxTokens)
            targetBranch.set(extension.targetBranch)
            fallbackCommits.set(extension.fallbackCommits)
            includePatterns.set(extension.includePatterns)
            excludePatterns.set(extension.excludePatterns)
            outputDir.set(extension.outputDir)
            outputFormat.set(extension.outputFormat)
            language.set(extension.language)
            failOnCritical.set(extension.failOnCritical)
            maxFileSize.set(extension.maxFileSize)
        }

        // Logging for local CLI
        val localTask = project.tasks.register("claudeReviewLocal", ClaudeReviewLocalTask::class.java)
        localTask.configure {
            group = "verification"
            description = "Run AI-powered code review using local Claude CLI"

            // Skip configuration
            claudePath.set(extension.claudePath)
            targetBranch.set(extension.targetBranch)
            fallbackCommits.set(extension.fallbackCommits)
            includePatterns.set(extension.includePatterns)
            excludePatterns.set(extension.excludePatterns)
            outputDir.set(extension.outputDir)
            outputFormat.set(extension.outputFormat)
            language.set(extension.language)
            failOnCritical.set(extension.failOnCritical)
            maxFileSize.set(extension.maxFileSize)
        }

        // Optional: integration with check
        project.afterEvaluate {
            if (extension.failOnCritical.get()) {
                project.tasks.findByName("check")?.dependsOn("claudeReview")
            }
        }
    }
}