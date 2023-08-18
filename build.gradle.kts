
val taboolib_version: String by project

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.50"
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
}

taboolib {
    install("common")
    install("common-5")
    install("module-chat")
    install("module-configuration")
    install("module-database")
    install("module-effect")
    install("module-kether")
    install("module-lang")
    install("module-metrics")
    install("module-nms")
    install("module-nms-util")
    install("expansion-command-helper")
    install("expansion-javascript")
    install("platform-bukkit")
    classifier = null
    version = taboolib_version

    description {
        contributors {
            name("Lanscarlos")
        }
        desc("A Kether Script Extension System for TabooLib")
        dependencies {
            name("Adyeshach").optional(true)
            name("Chemdah").optional(true)
            name("DungeonPlus").optional(true)
            name("Planners").optional(true)
            name("Invero").optional(true)
            name("Zaphkiel").optional(true)

            name("PlaceholderAPI").optional(true)
            name("LuckPerms").optional(true)
        }
    }
}

repositories {
    mavenLocal()
    maven { url = uri("https://repo.tabooproject.org/repository/releases/") }
    mavenCentral()
}

dependencies {

    compileOnly(kotlin("stdlib"))

    // server
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v11900:11900:universal")

    compileOnly("com.google.guava:guava:31.1-jre")

    // for kether
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") // 协程
    compileOnly("com.mojang:datafixerupper:4.0.26")
    compileOnly("net.luckperms:api:5.4")

    // other
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
        }
    }
}