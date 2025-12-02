# Claude AI Code Review Plugin

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/dev/surovtsev/claude-review/dev.surovtsev.claude-review.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=Plugin%20Portal)](https://plugins.gradle.org/plugin/dev.surovtsev.claude-review)

A Gradle plugin that automates code review using Claude AI. The plugin analyzes Git diffs and provides structured feedback about code quality, potential issues, and improvements in seconds.

## Features

- 🤖 **AI-Powered Reviews**: Uses Claude AI to analyze code changes
- 🔍 **Smart Diff Analysis**: Automatically detects changed files from Git
- 📝 **Multiple Output Formats**: Markdown, HTML, and JSON reports
- 🌍 **Multi-Language Support**: Reviews in English and Russian
- 🎯 **Flexible Filtering**: Include/exclude patterns for files
- 🚀 **Two Modes**: API mode and local Claude CLI mode
- ⚡ **CI/CD Ready**: Fail builds on critical issues

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("dev.surovtsev.claude-review") version "1.0.0"
}
```

Or using the legacy plugin application:

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.surovtsev:claude-review:1.0.0")
    }
}

apply plugin: "dev.surovtsev.claude-review"
```

## Configuration

### Basic Configuration

```kotlin
claudeReview {
    // API Configuration (required for API mode)
    apiKey = System.getenv("ANTHROPIC_API_KEY") // or set directly

    // Model selection
    model = "claude-3-5-sonnet-20241022" // default

    // Review language
    language = ClaudeReviewExtension.Language.ENGLISH // or RUSSIAN

    // Output configuration
    outputDir = "build/reviews"
    outputFormat = ClaudeReviewExtension.OutputFormat.MARKDOWN // or HTML, JSON
}
```

### Advanced Configuration

```kotlin
claudeReview {
    // Git Configuration
    targetBranch = "main" // default: auto-detect
    fallbackCommits = 10  // number of commits to review if branch detection fails

    // File Filtering
    includePatterns = listOf(
        "**/*.kt",
        "**/*.java",
        "**/*.ts",
        "**/*.js"
    )
    excludePatterns = listOf(
        "**/build/**",
        "**/generated/**",
        "**/*.test.kt"
    )

    // Behavior
    failOnCritical = true // fail build if critical issues found
    maxFileSize = 100_000L // skip files larger than this (in bytes)
    maxTokens = 4096 // maximum tokens for Claude response

    // Local CLI Configuration
    claudePath = "/usr/local/bin/claude" // path to Claude CLI (for local mode)
}
```

## Usage

### Running Code Review

#### Using Claude API

Set your Anthropic API key:
```bash
export ANTHROPIC_API_KEY="your-api-key-here"
```

Run the review:
```bash
./gradlew claudeReview
```

#### Using Local Claude CLI

If you have Claude CLI installed locally:
```bash
./gradlew claudeReviewLocal
```

### CI/CD Integration

#### GitHub Actions

```yaml
- name: Code Review
  env:
    ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
  run: ./gradlew claudeReview
```

#### Jenkins

```groovy
stage('Code Review') {
    environment {
        ANTHROPIC_API_KEY = credentials('anthropic-api-key')
    }
    steps {
        sh './gradlew claudeReview'
    }
}
```

### Review Output

The plugin generates detailed review reports in your specified format:

- **Markdown**: Human-readable format with syntax highlighting
- **HTML**: Web-friendly format with styling
- **JSON**: Machine-readable format for further processing

Reports are saved to `build/reviews/` by default.

## Example Output

```markdown
# Code Review Report

**Date**: 2024-12-02
**Branch**: feature/new-feature → main
**Files Reviewed**: 5

## Summary
✅ Overall code quality is good with minor improvements suggested.

## Issues Found

### Critical (0)
None

### Major (2)
1. **NullPointerException Risk** in `UserService.kt:45`
   - Missing null check for optional parameter

2. **SQL Injection Vulnerability** in `DatabaseQuery.kt:78`
   - Use parameterized queries instead of string concatenation

### Minor (3)
...
```

## Requirements

- Gradle 7.0 or higher
- Java 11 or higher
- Git repository
- Claude API key (for API mode) or Claude CLI (for local mode)

## Getting an API Key

1. Sign up at [Anthropic Console](https://console.anthropic.com/)
2. Generate an API key
3. Set it as an environment variable or in your build configuration

## Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/joleksiysurovtsev/claude-review-plugin.git
cd claude-review-plugin

# Build the plugin
./gradlew build

# Run tests
./gradlew test

# Publish to local Maven repository for testing
./gradlew publishToMavenLocal
```

### Versioning

The plugin uses [Semantic Versioning](https://semver.org/) and versions are managed automatically using git tags.

To create a new release:
```bash
# Patch release (bug fixes)
./release.sh patch

# Minor release (new features)
./release.sh minor

# Major release (breaking changes)
./release.sh major
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Testing

All PRs are automatically tested on:
- Java 11, 17, 21
- Ubuntu Linux

Tests must pass before merging.

## Support

- 🐛 **Issues**: [GitHub Issues](https://github.com/joleksiysurovtsev/claude-review-plugin/issues)
- 📧 **Contact**: [surovtsev.dev](https://surovtsev.dev/)
- 📖 **Documentation**: [Wiki](https://github.com/joleksiysurovtsev/claude-review-plugin/wiki)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Author

**Oleksii Surovtsev**
- Website: [surovtsev.dev](https://surovtsev.dev/)
- GitHub: [@joleksiysurovtsev](https://github.com/joleksiysurovtsev)

## Acknowledgments

- Powered by [Claude AI](https://www.anthropic.com/claude) by Anthropic
- Built with [Gradle](https://gradle.org/)