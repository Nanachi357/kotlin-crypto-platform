// Bybit Gateway Service - Build Configuration
// This build script uses Kotlin DSL for type-safe configuration

plugins {
    // Core Kotlin plugin for JVM compilation
    kotlin("jvm") version "2.2.0"
    
    // Kotlin serialization plugin for JSON handling  
    kotlin("plugin.serialization") version "2.2.0"
    
    // Ktor plugin for web server framework
    id("io.ktor.plugin") version "3.2.3"
    
    // Application plugin for executable JAR
    application
}

// Project Information
group = "com.github.nanachi357"
version = "0.1.0-SNAPSHOT"

// Java compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Repositories are defined in settings.gradle.kts

// Dependencies
dependencies {
    // === KTOR SERVER DEPENDENCIES ===
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-cors")
    
    // === KTOR CLIENT DEPENDENCIES ===
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging")
    
    // === JSON SERIALIZATION ===
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    
    // === LOGGING ===
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    
    // === TESTING ===
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

// Application configuration
application {
    mainClass.set("com.github.nanachi357.ApplicationKt")
    
    // Development mode JVM arguments
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Kotlin compilation options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",  // Strict null-safety
            "-opt-in=kotlin.RequiresOptIn"  // Allow experimental APIs
        )
    }
}

// Test configuration
tasks.withType<Test> {
    useJUnitPlatform()
}