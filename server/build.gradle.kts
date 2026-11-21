plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

dependencies {
    // Only the @Serializable domain models are used: the server never touches Compose, JGit or a LLM.
    implementation(project(":shared"))
    // Same BOM as the Koog client in :shared, so one Ktor version sits on the classpath.
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.kotlinx.serializationJson)
    runtimeOnly("org.slf4j:slf4j-nop:2.0.16") // Ktor logs through SLF4J; the server logs nothing on purpose

    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.testJunit)
}

application {
    mainClass = "dev.nitpick.server.MainKt"
}
