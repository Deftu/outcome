import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    val kotlinVersion = "2.3.0"
    kotlin("multiplatform") version(kotlinVersion) apply(false)
    kotlin("jvm") version(kotlinVersion) apply(false)

    val dgtVersion = "2.75.0"
    id("dev.deftu.gradle.tools") version(dgtVersion)
    id("dev.deftu.gradle.tools.publishing.maven") version(dgtVersion) apply(false)
}

subprojects {
    if (project.name == "outcome-slf4j") {
        return@subprojects
    }

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "dev.deftu.gradle.tools")
    apply(plugin = "dev.deftu.gradle.tools.publishing.maven")

    configure<KotlinMultiplatformExtension> {
        explicitApi()

        // --- JVM (Desktop, Android, Server) ---
        jvm {
            // Compile to Java 8 bytecode
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
            }

            withSourcesJar()
        }

        // --- JavaScript (Browser, Node.js) ---
        js(IR) {
            generateTypeScriptDefinitions()
            binaries.library()
            browser()
            nodejs()
        }

        // --- WebAssembly (Experimental) ---
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            generateTypeScriptDefinitions()
            binaries.library()
            browser()
        }

        // --- Native (Desktop + Apple ecosystem) ---
        linuxX64()         // Desktop Linux
        mingwX64()         // Windows native
        macosX64()         // macOS Intel (x86_64)
        macosArm64()       // macOS Apple Silicon (ARM64)

        // --- iOS ---
        iosArm64()         // iOS physical devices (ARM64)
        iosSimulatorArm64()// iOS simulator on Apple Silicon (ARM64)

        // --- tvOS ---
        tvosArm64()        // tvOS physical devices (Apple TV)
        tvosX64()          // tvOS simulator on Intel
        tvosSimulatorArm64() // tvOS simulator on Apple Silicon (ARM64)

        // --- watchOS ---
        watchosArm64()       // watchOS physical devices (Apple Watch)
        watchosX64()         // watchOS simulator on Intel
        watchosSimulatorArm64() // watchOS simulator on Apple Silicon (ARM64)

        sourceSets {
            commonMain.dependencies {
                if (project.name != "outcome-core") {
                    implementation(project(":outcome-core"))
                }
            }

            commonTest.dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }

            jvmMain.dependencies {
                implementation(kotlin("reflect"))
            }

            jvmTest.dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}
