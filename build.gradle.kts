plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.klin"
version = "1.3"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    
        val pluginProperties = mapOf(
            "main" to "com.klin.holoItems.HoloItems",
            "name" to "HoloItems",
            "version" to project.version,
            "apiVersion" to "1.20",
            "authors" to listOf("klin")
        )

        filesMatching("plugin.yml") {
            expand(pluginProperties)
        }
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.20.6")
    }
}
