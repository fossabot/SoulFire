plugins {
    id("fabric-loom") version "1.5.5"
    java
}

group = "net.pistonmaster"
version = "1.0.0"

repositories {
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings {
            nameSyntheticMembers = true
        }
        parchment("org.parchmentmc.data:parchment-1.20.3:2023.12.31@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}
