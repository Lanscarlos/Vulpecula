import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-applicative"))
    implementation(project(":project:common-bacikal"))
    implementation(project(":project:common-core"))
    implementation(project(":project:common-volatile"))
    implementation(project(":project:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("Vulpecula")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
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