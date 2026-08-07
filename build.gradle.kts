import io.izzel.taboolib.gradle.*

plugins {
    java
    id("io.izzel.taboolib") version "2.0.37"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitUI)
        install(BukkitUtil)
        install(BukkitNMS)
        install(BukkitNMSUtil)
        install(CommandHelper)
        install(Database)
        install(I18n)
        install(JavaScript)
        install(Kether)
        install(Metrics)
        install(MinecraftChat)
        install(MinecraftEffect)
    }
    version {
        taboolib = "6.3.0-75b18a2"
    }
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
//    maven("https://repo.spongepowered.org/maven")
//    maven("https://repo.tabooproject.org/repository/releases")
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
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_1_8
}
