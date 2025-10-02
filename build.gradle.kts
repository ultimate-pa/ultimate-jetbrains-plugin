plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "de.uni_freiburg.informatik.ultimate.intellij"
version = "1.0.0"

val ideVersion = "2025.1.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.20.0")

    intellijPlatform {
        // Use CLion for testing
        clion(ideVersion, useInstaller = true)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242" // Uses Java 21
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}
