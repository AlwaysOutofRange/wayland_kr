plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.outofrange"
version = "1.0-SNAPSHOT"


kotlin {
    jvmToolchain(23)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    targetCompatibility = "22"
}