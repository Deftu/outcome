kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(projects.outcomeCoroutines)
        }
    }
}
