import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val taboolib_version: String by project

dependencies {
    implementation("io.izzel.taboolib:common-5:$taboolib_version")
    implementation("io.izzel.taboolib:module-chat:$taboolib_version")
    implementation("io.izzel.taboolib:module-configuration:$taboolib_version")
    implementation("io.izzel.taboolib:module-effect:$taboolib_version")
    implementation("io.izzel.taboolib:module-lang:$taboolib_version")
    implementation("io.izzel.taboolib:module-kether:$taboolib_version")
    implementation("io.izzel.taboolib:module-nms:$taboolib_version")
    implementation("io.izzel.taboolib:module-nms-util:$taboolib_version")
    implementation("io.izzel.taboolib:module-metrics:$taboolib_version")
    implementation("io.izzel.taboolib:expansion-command-helper:$taboolib_version")
    implementation("io.izzel.taboolib:expansion-javascript:$taboolib_version")

    implementation(project(":project:common"))
    implementation(project(":project:common-core"))
    implementation(project(":project:module-applicative"))
    implementation(project(":project:module-bacikal"))
    implementation(project(":project:module-config"))
    implementation(project(":project:module-volatile"))
    implementation(project(":project:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("Vulpecula")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
        append("config.yml")
        append("lang/zh_CN.yml")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        // taboolib
        relocate("taboolib", "top.lanscarlos.vulpecula.taboolib")
        // kotlin
        relocate("kotlin.", "kotlin1531.") {
            exclude("kotlin.Metadata")
        }

    }
    kotlinSourcesJar {
        // include subprojects
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
    build {
        dependsOn(shadowJar)
    }
}

//publishing {
//    repositories {
//        maven {
//            url = uri("https://repo.tabooproject.org/repository/releases")
//            credentials {
//                username = project.findProperty("taboolibUsername").toString()
//                password = project.findProperty("taboolibPassword").toString()
//            }
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
//        }
//    }
//    publications {
//        create<MavenPublication>("library") {
//            from(components["java"])
//            groupId = project.group.toString()
//        }
//    }
//}