// Bybit Gateway Service - Build Configuration
// This build script uses Kotlin DSL for type-safe configuration

plugins {
    // Core Kotlin plugin for JVM compilation
    kotlin("jvm") version "1.9.22"
    
    // Kotlin serialization plugin for JSON handling  
    kotlin("plugin.serialization") version "1.9.22"
    
    // Ktor plugin for web server framework
    id("io.ktor.plugin") version "2.3.7"
    
    // Application plugin for executable JAR
    application
}

// Project Information
group = "io.swapter.bybit"
version = "0.1.0-SNAPSHOT"

// Java compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Repositories for dependency resolution
repositories {
    mavenCentral()
    google()
}

// Dependencies
dependencies {
    // === KTOR SERVER DEPENDENCIES ===
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    
    // === KTOR CLIENT DEPENDENCIES ===
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-content-negotiation")
    
    // === JSON SERIALIZATION ===
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    
    // === LOGGING ===
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // === TESTING ===
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

// Application configuration
application {
    mainClass.set("io.swapter.bybit.ApplicationKt")
    
    // Development mode JVM arguments
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Kotlin compilation options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-Xjsr305=strict",  // Strict null-safety
            "-opt-in=kotlin.RequiresOptIn"  // Allow experimental APIs
        )
    }
}

// Test configuration
tasks.withType<Test> {
    useJUnitPlatform()
}