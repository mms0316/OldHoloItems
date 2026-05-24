plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" //Access to Minecraft NMS + Paper API packages
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
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
            "apiVersion" to "26.1.2",
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
        minecraftVersion("26.1.2")
    }
}
