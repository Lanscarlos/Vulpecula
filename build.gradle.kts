import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.19"
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    taboolib {
        env {
            // 安装模块
            install(Basic, Bukkit, BukkitUtil, BukkitNMSUtil, Kether, CommandHelper)
        }
        version {
            taboolib = "6.2.0-beta31"
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
        maven("https://libraries.minecraft.net")
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.codemc.io/repository/nms/")
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    gradle.buildFinished {
        buildDir.deleteRecursively()
    }
}