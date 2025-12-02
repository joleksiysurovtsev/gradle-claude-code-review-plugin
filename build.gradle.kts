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
    tag {
        prefix.set("v")
        versionSeparator.set("")
    }
    versionIncrementer("incrementMinor")
}
version = scmVersion.version

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