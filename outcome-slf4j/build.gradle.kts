plugins {
    kotlin("jvm")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.publishing.maven")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(projects.outcomeCore)
    implementation(libs.slf4j.api)
}
