plugins {
    id("io.github.goooler.shadow") version "8.1.8"
    kotlin("jvm") version "2.0.0-Beta4"
    java
}

apply(plugin = "kotlin")
apply(plugin = "io.github.goooler.shadow")
group = "com.th7bo"
version = "1.0"
description = "Leaderboards plugin"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.github.honkling.commando:spigot:b909c9b")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.github.retrooper:packetevents-spigot:2.4.0")
}

tasks {
    withType(JavaCompile::class.java).forEach {
        it.options.compilerArgs.add("-source")
        it.options.compilerArgs.add("17")
        it.options.compilerArgs.add("-target")
        it.options.compilerArgs.add("21")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }

    processResources {
        val props =
            mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.19"
            )
        inputs.properties(props)
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") { expand(props) }
    }

    shadowJar {
        minimize()
        archiveFileName.set("Dynamo.jar")
        relocate("com.github.retrooper.packetevents", "com.th7bo.dynamo.packetevents.api")
        relocate("io.github.retrooper.packetevents", "com.th7bo.dynamo.packetevents.impl")
//        destinationDirectory.set(file("Server\\plugins\\"))
    }
}
