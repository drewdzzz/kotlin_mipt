import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.5"
    val koinVersion = "3.5.0"

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.test {
    useJUnitPlatform()
}

application {
    // Define the main class for the application.
    mainClass = "org.twittergram.MainKt"
}
