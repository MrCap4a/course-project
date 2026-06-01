plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "ru.denis"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "ru.denis.calculatordesktop.CalculatorApp"
}

repositories {
    mavenCentral()
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
}

// ── Single-file portable exe via 7-Zip SFX ───────────────────────────────────
// Requires: 7-Zip installed (any version) AND the 7-Zip Extra SFX module
//   1. Install 7-Zip from https://www.7-zip.org/
//   2. Download 7-Zip Extra from https://www.7-zip.org/download.html
//      and copy 7zSD.sfx → C:\Program Files\7-Zip\7zSD.sfx
// Usage: .\gradlew.bat packageSfx
// Result: build\package\Calculator-portable.exe  (single file, ~120 MB)
// ─────────────────────────────────────────────────────────────────────────────
tasks.register("packageSfx") {
    group = "distribution"
    description = "Creates a single portable .exe via 7-Zip SFX (requires 7-Zip + 7zSD.sfx)"
    dependsOn("packageApp")

    doLast {
        val sevenZip  = file("C:/Program Files/7-Zip/7z.exe")
        val sfxModule = file("C:/Program Files/7-Zip/7zSD.sfx")
        val appDir    = layout.buildDirectory.dir("package/Calculator").get().asFile
        val outDir    = layout.buildDirectory.dir("package").get().asFile
        val archiveFile = outDir.resolve("Calculator.7z")
        val outputExe   = outDir.resolve("Calculator-portable.exe")
        val configFile  = outDir.resolve("sfx-config.txt")

        check(sevenZip.exists())  { "7-Zip not found at ${sevenZip.absolutePath}" }
        check(sfxModule.exists()) { "7zSD.sfx not found — download 7-Zip Extra from https://www.7-zip.org/download.html" }

        // SFX config: silent extraction + auto-run
        configFile.writeText(
            ";!@Install@!UTF-8!\n" +
            "Title=\"Calculator\"\n" +
            "RunProgram=\"Calculator\\Calculator.exe\"\n" +
            "GUIMode=\"2\"\n" +
            ";!@InstallEnd@!\n"
        )

        // Pack the app-image folder into a 7z archive
        val process = ProcessBuilder(
                sevenZip.absolutePath, "a", "-t7z", "-mx=5",
                archiveFile.absolutePath, "${appDir.absolutePath}/*")
            .inheritIO()
            .start()
        check(process.waitFor() == 0) { "7-Zip compression failed" }

        // Combine: SFX stub + config + archive → single exe
        outputExe.outputStream().use { out ->
            sfxModule.inputStream().use { it.copyTo(out) }
            configFile.inputStream().use { it.copyTo(out) }
            archiveFile.inputStream().use { it.copyTo(out) }
        }

        archiveFile.delete()
        configFile.delete()
        println("✓ Single exe created: ${outputExe.absolutePath}")
    }
}

// ── Native packaging ─────────────────────────────────────────────────────────
// Produces a self-contained folder in build/package/Calculator/
// with an embedded JRE — no installation required on target machine.
//
// Usage:
//   .\gradlew.bat packageApp              → app-image folder (no extra tools)
//   .\gradlew.bat packageApp -Ptype=exe   → Windows .exe installer (needs WiX 3 in PATH)
//   .\gradlew.bat packageApp -Ptype=msi   → Windows .msi installer (needs WiX 3 in PATH)
//
// WiX 3 download: https://github.com/wixtoolset/wix3/releases
// ─────────────────────────────────────────────────────────────────────────────
tasks.register<Exec>("packageApp") {
    group = "distribution"
    description = "Package the app using jpackage (bundled JRE, no install needed for app-image)"
    dependsOn("installDist")

    val javaHome = System.getProperty("java.home")
        ?: error("java.home is not set")
    val pkgType  = (project.findProperty("type") as String?) ?: "app-image"
    val libDir   = layout.buildDirectory.dir("install/CalculatorDesktop/lib").get().asFile
    val outDir   = layout.buildDirectory.dir("package").get().asFile

    doFirst {
        outDir.mkdirs()
        // jpackage fails if the output folder already exists for the app
        outDir.resolve("Calculator").deleteRecursively()
    }

    commandLine(
        "$javaHome/bin/jpackage",
        "--type",         pkgType,
        "--name",         "Calculator",
        "--app-version",  "1.0.0",
        "--input",        libDir.absolutePath,
        "--main-jar",     "CalculatorDesktop-${project.version}.jar",
        "--main-class",   "ru.denis.calculatordesktop.CalculatorApp",
        "--module-path",  libDir.absolutePath,
        "--add-modules",  "javafx.controls,javafx.fxml,javafx.graphics,javafx.base," +
                          "java.net.http,java.logging",
        "--dest",         outDir.absolutePath,
        "--java-options", "-Dfile.encoding=UTF-8"
    )
}
