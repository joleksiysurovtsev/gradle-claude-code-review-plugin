# Claude Review Plugin - Руководство по настройке

## 📦 Установка плагина

### Шаг 1: Настройка репозиториев

В файле **`settings.gradle.kts`** вашего проекта:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()         // Для локально установленного плагина
        gradlePluginPortal() // Для официальных плагинов Gradle
    }
}

rootProject.name = "your-project-name"
```

### Шаг 2: Подключение плагина

В файле **`build.gradle.kts`** вашего проекта:

```kotlin
plugins {
    // Ваши существующие плагины
    kotlin("jvm") version "1.9.0"

    // Добавьте плагин Claude Review
    id("com.flo.claude-review") version "0.1.1"
}
```

## 🎯 Базовая настройка

### Минимальная конфигурация

```kotlin
claudeReview {
    // Для API версии (задача claudeReview)
    apiKey.set(System.getenv("ANTHROPIC_API_KEY") ?: "")

    // Для локальной версии (задача claudeReviewLocal)
    // настройка не требуется - плагин найдет Claude автоматически
}
```

### Рекомендуемая конфигурация

```kotlin
claudeReview {
    // Целевая ветка для сравнения
    targetBranch.set("develop")  // или "main", "master", "auto"

    // Язык отчетов
    language.set(com.flo.gradle.claudereview.ClaudeReviewExtension.Language.RUSSIAN)

    // Формат отчетов
    outputFormat.set(com.flo.gradle.claudereview.ClaudeReviewExtension.OutputFormat.HTML)

    // Фильтры файлов
    includePatterns.set(listOf(
        "**/*.kt",
        "**/*.java",
        "**/*.ts",
        "**/*.tsx"
    ))

    excludePatterns.set(listOf(
        "**/build/**",
        "**/node_modules/**",
        "**/*.generated.*"
    ))
}
```

## 📝 Полная конфигурация

```kotlin
claudeReview {
    // ==========================================
    // 🔧 Общие настройки
    // ==========================================

    // Целевая ветка для diff сравнения
    // Варианты: "develop", "main", "master", "auto" (автоопределение)
    targetBranch.set("develop")

    // Количество коммитов для анализа (если ветка не найдена)
    fallbackCommits.set(5)

    // Максимальный размер файла для анализа (байты)
    maxFileSize.set(100_000L)

    // ==========================================
    // 📂 Фильтрация файлов
    // ==========================================

    // Паттерны файлов для включения в анализ
    includePatterns.set(listOf(
        "**/*.kt",           // Kotlin
        "**/*.java",         // Java
        "**/*.ts",           // TypeScript
        "**/*.tsx",          // React TypeScript
        "**/*.js",           // JavaScript
        "**/*.jsx",          // React JavaScript
        "**/*.py",           // Python
        "**/*.go",           // Go
        "**/*.rs",           // Rust
        "**/*.gradle.kts",   // Gradle Kotlin DSL
        "**/*.gradle"        // Gradle Groovy
    ))

    // Паттерны файлов для исключения из анализа
    excludePatterns.set(listOf(
        "**/build/**",           // Build директории
        "**/out/**",             // Output директории
        "**/target/**",          // Maven target
        "**/.gradle/**",         // Gradle кэш
        "**/node_modules/**",    // NPM пакеты
        "**/vendor/**",          // Vendor директории
        "**/*.generated.*",      // Сгенерированные файлы
        "**/*.min.js",          // Минифицированный JS
        "**/dist/**",           // Distribution файлы
        "**/.idea/**",          // IDE файлы
        "**/.vscode/**"         // IDE файлы
    ))

    // ==========================================
    // 📊 Настройки отчетов
    // ==========================================

    // Директория для сохранения отчетов
    outputDir.set("build/code-reviews")

    // Формат отчета
    // Варианты: MARKDOWN, HTML, JSON
    outputFormat.set(com.flo.gradle.claudereview.ClaudeReviewExtension.OutputFormat.HTML)

    // Язык отчетов
    // Варианты: RUSSIAN, ENGLISH
    language.set(com.flo.gradle.claudereview.ClaudeReviewExtension.Language.RUSSIAN)

    // ==========================================
    // ⚙️ Поведение плагина
    // ==========================================

    // Падать при обнаружении критических проблем
    // Полезно для CI/CD пайплайнов
    failOnCritical.set(false)

    // ==========================================
    // 🔑 Настройки для Claude API
    // ==========================================

    // API ключ Anthropic (для задачи claudeReview)
    // Рекомендуется использовать переменные окружения
    apiKey.set(providers.environmentVariable("ANTHROPIC_API_KEY").orElse(""))

    // Модель Claude для использования
    model.set("claude-3-5-sonnet-20241022")

    // Максимальное количество токенов в ответе
    maxTokens.set(2048)

    // ==========================================
    // 💻 Настройки для локального CLI
    // ==========================================

    // Путь к Claude CLI (для задачи claudeReviewLocal)
    // Пустая строка = автоматический поиск
    claudePath.set("")

    // Или укажите явный путь:
    // claudePath.set("/usr/local/bin/claude")
    // claudePath.set("${System.getProperty("user.home")}/.nvm/versions/node/v20.0.0/bin/claude")
}
```

## 🚀 Доступные задачи

После настройки плагина вам будут доступны следующие Gradle задачи:

### claudeReview 
Запускает код-ревью через Claude API

```bash
# Установите API ключ
export ANTHROPIC_API_KEY="sk-ant-api03-..."

# Запустите ревью
./gradlew claudeReview
```

### claudeReviewLocal
Запускает код-ревью через локально установленный Claude CLI

```bash
# Установите Claude CLI (один раз)
npm install -g @anthropic-ai/claude-cli
claude login

# Запустите локальное ревью
./gradlew claudeReviewLocal
```

## 🎨 Примеры конфигураций

### Для Kotlin/Android проекта

```kotlin
claudeReview {
    targetBranch.set("develop")

    includePatterns.set(listOf(
        "**/*.kt",
        "**/*.xml",
        "**/*.gradle.kts"
    ))

    excludePatterns.set(listOf(
        "**/build/**",
        "**/generated/**",
        "**/*Test.kt",
        "**/test/**"
    ))

    language.set(com.flo.gradle.claudereview.ClaudeReviewExtension.Language.RUSSIAN)
    outputFormat.set(com.flo.gradle.claudereview.ClaudeReviewExtension.OutputFormat.HTML)
}
```

### Для TypeScript/React проекта

```kotlin
claudeReview {
    targetBranch.set("main")

    includePatterns.set(listOf(
        "**/*.ts",
        "**/*.tsx",
        "**/*.js",
        "**/*.jsx",
        "**/*.css",
        "**/*.scss"
    ))

    excludePatterns.set(listOf(
        "**/node_modules/**",
        "**/dist/**",
        "**/build/**",
        "**/*.min.js",
        "**/*.test.ts",
        "**/*.spec.ts"
    ))

    language.set(com.flo.gradle.claudereview.ClaudeReviewExtension.Language.ENGLISH)
}
```

### Для микросервисов

```kotlin
claudeReview {
    targetBranch.set("master")

    includePatterns.set(listOf(
        "**/src/main/**/*.java",
        "**/src/main/**/*.kt",
        "**/api/**/*.yaml",
        "**/docker/**"
    ))

    excludePatterns.set(listOf(
        "**/target/**",
        "**/build/**",
        "**/test/**"
    ))

    failOnCritical.set(true)  // Строгий режим для production
}
```

## 🔧 Кастомные задачи

### Задача для Pull Request

```kotlin
tasks.register("prReview") {
    group = "verification"
    description = "Code review for pull requests"

    doFirst {
        claudeReview {
            targetBranch.set("origin/${System.getenv("GITHUB_BASE_REF") ?: "main"}")
            failOnCritical.set(true)
        }
    }

    dependsOn("claudeReviewLocal")
}
```

### Задача для быстрого ревью

```kotlin
tasks.register("quickReview") {
    group = "verification"
    description = "Quick review of recent changes"

    doFirst {
        claudeReview {
            fallbackCommits.set(1)  // Только последний коммит
            outputFormat.set(com.flo.gradle.claudereview.ClaudeReviewExtension.OutputFormat.MARKDOWN)
        }
    }

    dependsOn("claudeReviewLocal")
}
```

### Задача для критических файлов

```kotlin
tasks.register("securityReview") {
    group = "verification"
    description = "Security-focused code review"

    doFirst {
        claudeReview {
            includePatterns.set(listOf(
                "**/security/**",
                "**/auth/**",
                "**/crypto/**",
                "**/*Config.kt",
                "**/*Security*.java"
            ))
            failOnCritical.set(true)
        }
    }

    dependsOn("claudeReview")
}
```

## 🔄 Интеграция с CI/CD

### GitHub Actions

```yaml
name: Code Review

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  review:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 0

    - uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Code Review
      run: ./gradlew claudeReview
      env:
        ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}

    - name: Upload Report
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: review-report
        path: build/code-reviews/
```

### GitLab CI

```yaml
code_review:
  stage: test
  script:
    - ./gradlew claudeReview
  variables:
    ANTHROPIC_API_KEY: $ANTHROPIC_API_KEY
  artifacts:
    when: always
    paths:
      - build/code-reviews/
    expire_in: 1 week
  only:
    - merge_requests
```

### Jenkins

```groovy
pipeline {
    agent any

    environment {
        ANTHROPIC_API_KEY = credentials('anthropic-api-key')
    }

    stages {
        stage('Code Review') {
            steps {
                sh './gradlew claudeReview'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'build/code-reviews/**',
                                     allowEmptyArchive: true
                }
            }
        }
    }
}
```

## ⚡ Советы по производительности

1. **Используйте фильтры файлов** для исключения ненужных файлов
2. **Ограничьте размер файлов** через `maxFileSize`
3. **Для больших PR** используйте `fallbackCommits` для анализа только последних изменений
4. **Кэшируйте зависимости** в CI/CD для ускорения сборки

## 🐛 Решение проблем

### API ключ не работает
```bash
# Проверьте, что ключ установлен
echo $ANTHROPIC_API_KEY

# Или установите в gradle.properties
echo "anthropicApiKey=sk-ant-api03-..." >> ~/.gradle/gradle.properties

# И используйте в build.gradle.kts
apiKey.set(project.findProperty("anthropicApiKey")?.toString() ?: "")
```

### Claude CLI не найден
```bash
# Установите глобально
npm install -g @anthropic-ai/claude-cli

# Или укажите путь явно
which claude  # найдите путь
# Затем в build.gradle.kts:
claudePath.set("/path/to/claude")
```

### Нет изменений для ревью
```bash
# Проверьте, что есть изменения
git diff develop --name-only

# Или явно укажите ветку
./gradlew claudeReview -PtargetBranch=main
```

## 📚 Дополнительные ресурсы

- [Исходный код плагина](https://github.com/flo/claude-review-plugin)
- [Anthropic API документация](https://docs.anthropic.com)
- [Claude CLI документация](https://github.com/anthropics/claude-cli)

## 📄 Лицензия

MIT License - свободно используйте в своих проектах!