pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.22"
    }

    repositories {
        gradlePluginPortal()
        maven ("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "Dynamo"

//sequenceOf(
//    "Core",
//    "V1_20",
//    "Common"
//).forEach {
//    include("${rootProject.name}-$it")
//    project(":${rootProject.name}-$it").projectDir = file(it)
//}