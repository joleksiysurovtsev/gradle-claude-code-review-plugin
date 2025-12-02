plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.1"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("pl.allegro.tech.build.axion-release") version "1.16.1"
}

group = "dev.surovtsev"


scmVersion {
    // Настройка тегов
    tag {
        prefix.set("v")
        versionSeparator.set("")
    }

    // Стратегия версионирования
    versionIncrementer("incrementPatch") // По умолчанию patch релизы

    // Убираем SNAPSHOT для релизных веток
    branchVersionIncrementer.put("release", "incrementPrerelease")
    branchVersionIncrementer.put("release/.*", "incrementPrerelease")

    // Версия для неотмеченных коммитов
    snapshotCreator { version, _ ->
        if (version.contains("-")) {
            version
        } else {
            "$version-SNAPSHOT"
        }
    }

    // Проверки
    checks {
        uncommittedChanges.set(false) // Разрешаем незакоммиченные изменения
        aheadOfRemote.set(false) // Разрешаем локальные коммиты
    }

    // Хуки
    hooks {
        pre("fileUpdate", mapOf(
            "file" to "README.md",
            "pattern" to "version \"\\d+\\.\\d+\\.\\d+\"",
            "replacement" to "version \"${version}\""
        ))
    }
}

// Используем версию из git тегов
project.version = scmVersion.version

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://surovtsev.dev/")
    vcsUrl.set("https://github.com/joleksiysurovtsev/claude-review-plugin")

    plugins {
        register("claudeReview") {
            id = "dev.surovtsev.claude-review"
            displayName = "Claude AI Code Review"
            description = "Gradle plugin that automates code review using Claude AI. Analyzes Git diffs and provides structured feedback about code quality, potential issues, and improvements."
            tags.set(listOf("code-review", "ai", "claude", "static-analysis", "code-quality", "automation"))
            implementationClass = "dev.surovtsev.claudereview.ClaudeReviewPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        mavenLocal()
    }
}

tasks.register("publishToLocal") {
    group = "publishing"
    description = "Publishes the plugin to Maven Local repository"
    dependsOn("publishToMavenLocal")
    doLast {
        println("✅ Plugin published to Maven Local")
        println("📦 Group: ${project.group}")
        println("📦 Name: ${project.name}")
        println("📦 Version: ${project.version}")
        println("")
        println("To use in another project, add to settings.gradle.kts:")
        println("  pluginManagement {")
        println("    repositories {")
        println("      mavenLocal()")
        println("      gradlePluginPortal()")
        println("    }")
        println("  }")
        println("")
        println("And in build.gradle.kts:")
        println("  plugins {")
        println("    id(\"dev.surovtsev.claude-review\") version \"${project.version}\"")
        println("  }")
    }
}

// Таски для управления версиями
tasks.register("nextPatchVersion") {
    group = "release"
    description = "Bumps to next patch version"
    doLast {
        exec {
            commandLine("./gradlew", "release", "-Prelease.versionIncrementer=incrementPatch")
        }
    }
}

tasks.register("nextMinorVersion") {
    group = "release"
    description = "Bumps to next minor version"
    doLast {
        exec {
            commandLine("./gradlew", "release", "-Prelease.versionIncrementer=incrementMinor")
        }
    }
}

tasks.register("nextMajorVersion") {
    group = "release"
    description = "Bumps to next major version"
    doLast {
        exec {
            commandLine("./gradlew", "release", "-Prelease.versionIncrementer=incrementMajor")
        }
    }
}

tasks.register("showVersion") {
    group = "release"
    description = "Shows current project version"
    doLast {
        println("Current version: ${project.version}")
        val isSnapshot = project.version.toString().contains("SNAPSHOT")
        if (isSnapshot) {
            println("⚠️ This is a SNAPSHOT version")
        } else {
            println("✅ This is a release version")
        }
    }
}


gitProperties {
    gitPropertiesName = "git.properties"
    keys = listOf(
        "git.branch",
        "git.commit.id",
        "git.commit.id.abbrev",
        "git.commit.time",
        "git.tags",
        "git.build.version"
    )
}


System.getenv("GRADLE_PUBLISH_KEY")?.let {
    System.setProperty("gradle.publish.key", it)
}
System.getenv("GRADLE_PUBLISH_SECRET")?.let {
    System.setProperty("gradle.publish.secret", it)
}