import io.izzel.taboolib.gradle.*
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.MinecraftChat
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.I18n
import io.izzel.taboolib.gradle.Kether

plugins {
    java
    id("io.izzel.taboolib") version "2.0.23"
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    taboolib {
        env {
            // 安装模块
            install(Basic)
            install(Bukkit)
            install(BukkitNMS)
            install(BukkitNMSUtil)
            install(BukkitNMSDataSerializer)
            install(BukkitUI)
            install(BukkitUtil)
            install(CommandHelper)
            install(I18n)
            install(JavaScript)
            install(Kether)
            install(Metrics)
            install(MinecraftChat)
        }
        version {
            taboolib = "6.2.3-12d4045"
        }
        description {
            name(rootProject.name)
            contributors {
                name("Lanscarlos")
            }
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
        maven("https://maven.aliyun.com/repository/central")
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

}
