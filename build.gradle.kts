plugins {
    kotlin("jvm") version "2.0.20"
    id("c")
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
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    targetCompatibility = "22"
}