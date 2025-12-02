package dev.surovtsev.claudereview.git

import java.io.File

class GitDiffProvider(
    private val projectDir: File,
    private val targetBranch: String,
    private val fallbackCommits: Int
) {
    var resolvedBranch: String = targetBranch
        private set

    fun getChangedFiles(): List<String> {
        resolvedBranch = resolveTargetBranch()

        val output = executeGit("diff", "--name-only", "$resolvedBranch...HEAD")

        return output.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun buildContext(changedFiles: List<String>, maxFileSize: Long): String {
        val sb = StringBuilder()

        sb.appendLine("# Code Review Context")
        sb.appendLine("Target branch: $resolvedBranch")
        sb.appendLine()
        sb.appendLine("## Changed Files:")
        changedFiles.forEach { sb.appendLine("- $it") }
        sb.appendLine()

        // contents of the files
        changedFiles.forEach { filePath ->
            val file = File(projectDir, filePath)
            if (file.exists() && file.length() < maxFileSize) {
                sb.appendLine("## File: $filePath")
                sb.appendLine("```${file.extension}")
                sb.appendLine(file.readText())
                sb.appendLine("```")
                sb.appendLine()
            }
        }

        // Git diff
        val diff = executeGit("diff", "$resolvedBranch...HEAD")
        sb.appendLine("## Git Diff:")
        sb.appendLine("```diff")
        sb.appendLine(diff.take(50_000)) // limit the size
        sb.appendLine("```")

        return sb.toString()
    }

    private fun resolveTargetBranch(): String {
        if (targetBranch != "auto") {
            if (branchExists(targetBranch)) {
                return targetBranch
            }
            throw IllegalStateException("Branch '$targetBranch' not found")
        }

        // auto-detection
        val candidates = listOf(
            "origin/develop",
            "origin/master",
            "origin/main",
            "develop",
            "master",
            "main"
        )

        candidates.forEach { branch ->
            if (branchExists(branch)) {
                return branch
            }
        }

        // Fallback on the last N Kommitts
        return "HEAD~$fallbackCommits"
    }

    private fun branchExists(branch: String): Boolean {
        return try {
            val result = executeGit("rev-parse", "--verify", branch)
            result.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    private fun executeGit(vararg args: String): String {
        val process = ProcessBuilder(listOf("git") + args)
            .directory(projectDir)
            .redirectErrorStream(false)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("Git command failed: git ${args.joinToString(" ")}\n$error")
        }

        return output.trim()
    }
}
