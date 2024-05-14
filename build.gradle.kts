import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.11"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    taboolib {
        env {
            install(
                UNIVERSAL,
                DATABASE,
                EFFECT,
                NMS_UTIL,
                KETHER,
                UI,
                NAVIGATION,
                METRICS,
                BUKKIT_ALL
            )
        }
        version {
            taboolib = "6.1.1-beta27"
        }
    }

    repositories {
//        mavenLocal()
        maven { url = uri("https://repo.spongepowered.org/maven") }
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

//    java {
//        withSourcesJar()
//    }
//
//    tasks.withType<JavaCompile> {
//        options.encoding = "UTF-8"
//    }
//
//    tasks.withType<KotlinCompile> {
//        kotlinOptions {
//            jvmTarget = "1.8"
//            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
//        }
//    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}