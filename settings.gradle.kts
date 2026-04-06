import groovy.lang.MissingPropertyException

pluginManagement {
    repositories {
        // Snapshots
        maven("https://maven.deftu.dev/snapshots")
        maven("https://s01.oss.sonatype.org/content/groups/public/")

        // Repositories
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://jitpack.io/")

        // Default repositories
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val projectName = extra["project.name"]?.toString() ?: throw MissingPropertyException("The project name was not configured!")
rootProject.name = projectName

listOf(
    "core",
    "coroutines",
    "test",
    "retry",
    "slf4j",
    "ktor-client",
    "ktor-server",
    "jda-ktx",
    "jda-akuma",
).forEach { module ->
    file("$projectName-$module").takeIf { !it.exists() }?.mkdirs()
    include(":$projectName-$module")
}
