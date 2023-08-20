import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val taboolib_version: String by project

plugins {
    `maven-publish`
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("http://ptms.ink:8081/repository/releases/")
            isAllowInsecureProtocol = true
        }
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        // taboolib
        compileOnly("io.izzel.taboolib:common:$taboolib_version")
        compileOnly("io.izzel.taboolib:common-5:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-chat:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-configuration:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-database:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-effect:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-kether:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-lang:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-metrics:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-nms:$taboolib_version")
        compileOnly("io.izzel.taboolib:module-nms-util:$taboolib_version")
        compileOnly("io.izzel.taboolib:expansion-command-helper:$taboolib_version")
        compileOnly("io.izzel.taboolib:expansion-javascript:$taboolib_version")
        compileOnly("io.izzel.taboolib:platform-bukkit:$taboolib_version")
    }

    java {
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
}