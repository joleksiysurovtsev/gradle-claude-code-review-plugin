package dev.surovtsev.claudereview

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

class ClaudeReviewPluginTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")

        // Init git repo
        ProcessBuilder("git", "init")
            .directory(testProjectDir)
            .start()
            .waitFor()

        ProcessBuilder("git", "config", "user.email", "test@test.com")
            .directory(testProjectDir)
            .start()
            .waitFor()

        ProcessBuilder("git", "config", "user.name", "Test")
            .directory(testProjectDir)
            .start()
            .waitFor()
    }

    @Test
    fun `plugin registers task`() {
        buildFile.writeText("""
            plugins {
                id("dev.surovtsev.claude-review")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group=verification")
            .build()

        assertTrue(result.output.contains("claudeReview"))
    }

    @Test
    fun `extension configures task`() {
        buildFile.writeText("""
            plugins {
                id("dev.surovtsev.claude-review")
            }

            claudeReview {
                model.set("claude-haiku-4-5-20251001")
                language.set(dev.surovtsev.claudereview.ClaudeReviewExtension.Language.ENGLISH)
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("help")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }
}