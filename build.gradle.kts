plugins {
    kotlin("jvm") version "2.0.20"
    id("c")

    application
}

group = "com.outofrange"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
}

tasks.withType<JavaCompile> {
    targetCompatibility = "22"
}

kotlin {
    jvmToolchain(23)
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED"
    )
}