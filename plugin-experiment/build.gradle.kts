import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.Base64

taboolib {
    subproject = false
    relocate("kotlinx.metadata.", "kotlinx.metadata060.")
    relocate("com.ucasoft.kcron.", "com.ucasoft.kcron0230.")
    relocate("io.foldright.cffu.", "io.foldright.cffu113.")
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-config"))
    compileOnly(project(":common-core"))
    compileOnly(project(":module-action-event"))
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module-command"))
    compileOnly(project(":module-core"))
    compileOnly(project(":module-dispatcher"))
    compileOnly(project(":module-schedule"))
    compileOnly(project(":module-script"))
    compileOnly(project(":platform-bukkit"))
}

tasks {
    register("clean-workspace") {
        delete(layout.buildDirectory.dir("workspace"))
    }

    register<Copy>("embed-action") {
        dependsOn(":module-action-event:jar")
        into(layout.buildDirectory.dir("workspace/action"))
        val dependencies = configurations["compileOnly"].dependencies.filterIsInstance<ProjectDependency>()
        for (dependency in dependencies) {
            if (!dependency.dependencyProject.name.startsWith("module-action-")) {
                // 排除非拓展语句资源
                continue
            }
            from(dependency.dependencyProject.tasks.getByName<Jar>("jar").archiveFile)
        }
    }

    register("merge-resources") {
        val workspace = file(layout.buildDirectory.dir("workspace")).also(File::mkdirs)
        val resources = mutableMapOf<String, File>()
        val dependencies = configurations["compileOnly"].dependencies.filterIsInstance<ProjectDependency>()
        for (dependency in dependencies) {
            if (dependency.dependencyProject.name.startsWith("module-action-")) {
                // 排除拓展语句资源
                continue
            }
            val files = files(dependency.dependencyProject.sourceSets["main"].resources)
            for (file in files) {
                val name = file.absolutePath.substringAfter("resources\\")
                val resource = resources.computeIfAbsent(name) { File(workspace, name) }
                if (!resource.parentFile.exists()) {
                    resource.parentFile.mkdirs()
                }
                resource.appendText("\n\n")
                resource.appendBytes(file.readBytes())
            }
        }
    }

    register("asm-analyse") {
        dependsOn(":module-action-event:jar")
        doLast {
            val workspace = file(layout.buildDirectory.dir("workspace/metadata")).also(File::mkdirs)
            val files = configurations["compileOnly"].dependencies
                .filterIsInstance<ProjectDependency>()
                .filter { it.dependencyProject.name.startsWith("module-action-") }
                .flatMap { it.dependencyProject.sourceSets["main"].output.classesDirs }
                .filter { it.exists() }
                .flatMap { it.walk().onEnter { file -> file.name != "META-INF" } }
                .filter { it.isFile && it.extension == "class" }

            // ASM 解析
            for (file in files) {
                val reader = ClassReader(file.readBytes())
                val data = mutableListOf<String>()
                reader.accept(object : ClassVisitor(Opcodes.ASM9) {
                    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
                        if (descriptor != "Lkotlin/Metadata;") {
                            return null
                        }
                        return object : AnnotationVisitor(Opcodes.ASM9) {
                            override fun visitArray(name: String?): AnnotationVisitor? {
                                if (name != "d1") {
                                    return null
                                }
                                return object : AnnotationVisitor(Opcodes.ASM9) {
                                    override fun visit(name: String?, value: Any?) {
                                        if (value !is String) {
                                            data += ""
                                            return
                                        }
                                        val base64 = Base64.getEncoder().encodeToString(value.toByteArray(Charsets.ISO_8859_1))
                                        data += base64
                                    }
                                }
                            }
                        }
                    }
                }, 0)

                val name = reader.className.replace('/', '.') + ".metadata"
                File(workspace, name).writeText(data.joinToString("\n"))
            }
        }
    }

    jar {
        archiveBaseName.set("${rootProject.name}-experiment")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))

        dependsOn("clean-workspace")
        dependsOn("embed-action")
        dependsOn("merge-resources")
        dependsOn("asm-analyse")

        // 打包资源文件
        from(layout.buildDirectory.dir("workspace")) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        // 打包子项目源码
        val dependencies = configurations["compileOnly"].dependencies.filterIsInstance<ProjectDependency>()
        for (dependency in dependencies) {
            if (dependency.dependencyProject.name.startsWith("module-action-")) {
                // 排除拓展语句
                continue
            }
            from(dependency.dependencyProject.sourceSets["main"].output) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }

    }
}