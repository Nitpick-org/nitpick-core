import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "dev.nitpick.app.MainKt"

        // `gradlew :desktopApp:run` is a plain `java` process: without these flags the
        // macOS dock shows Duke and the name "java". Packaged apps get both from the bundle.
        if (System.getProperty("os.name").startsWith("Mac")) {
            jvmArgs(
                "-Xdock:name=Nitpick",
                // Not the shared full-bleed artwork: macOS icons carry ~18% transparent
                // margin in the file itself, or the dock tile looks oversized.
                "-Xdock:icon=${project.file("dock-icon.png")}",
            )
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.nitpick.app"
            packageVersion = "1.0.0"
        }
    }
}