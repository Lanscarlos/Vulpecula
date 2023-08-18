import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.zip.ZipFile

dependencies {
    implementation(project(":project:common-legacy"))
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("Vulpecula")
        archiveClassifier.set("")
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
    create("collect") {
        doFirst {
            val version = project.version
            val file = projectDir.resolve("build/libs/plugin-$version.jar")
            val newFile = projectDir.resolve("build/libs/${rootProject.name}-$version.jar")
            ZipFile(file).use { old ->
                ZipOutputStream(FileOutputStream(newFile)).use { new ->
                    for (entry in old.entries()) {
                        new.putNextEntry(entry)
                        if (!entry.isDirectory) {
                            new.write(old.getInputStream(entry).readBytes())
                        }
                        new.closeEntry()
                    }

                    // 因为 TabooLib 运行在 relocated 后的 Kotlin 环境中 (kotlin1820)
                    // 因此需要给脚本提供未经重定向的 jar 文件来进行编译

                    // 运行环境及标准库
                    applyToZip(new, version, "runtime/core", "common-core")
                }
            }
            file.delete()
        }
    }
    build {
        dependsOn(shadowJar)
    }
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

fun applyToZip(new: ZipOutputStream, version: Any, name: String, module: String) {
    new.putNextEntry(JarEntry("$name.jar"))
    new.write(rootProject.file("project/$module/build/libs/$module-$version.jar").readBytes())
    new.closeEntry()
}