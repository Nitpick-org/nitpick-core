plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

dependencies {
    // Only the @Serializable domain classes are used, but project(":shared") still pulls
    // Compose, JGit and Koog transitively; a narrow :domain module is post-freeze. Do not
    // claim a slim server in the pitch until that module exists.
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

distributions {
    main {
        contents {
            // :shared brings androidx and JetBrains lifecycle jars with the same file name; the server uses neither.
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}
