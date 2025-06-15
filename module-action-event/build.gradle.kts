import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.Base64

version = "1.0.0"

taboolib {
    subproject = false

    description {
        name("Vulpecula-Action-" + project.name.substringAfterLast('-').uppercaseFirstChar())
        desc("Please put this action jar in directory `./plugins/Vulpecula/action/` of your server.")
        contributors {
            this.contributors.clear()
            name("Lanscarlos")
        }
        dependencies {
            this.dependencies.clear()
            name("DISABLE")
        }
    }
}

dependencies {
    compileOnly(project(":module-bacikal"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
}

tasks {
    register("asm-analyse") {
        dependsOn("classes")
        doLast {
            val workspace = file(layout.buildDirectory.dir("workspace/metadata")).also(File::mkdirs)
            val files = project.sourceSets["main"].output.classesDirs
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
        archiveBaseName.set(project.name.substringAfter('-'))
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs/action"))

        dependsOn("asm-analyse")

        from(layout.buildDirectory.dir("workspace")) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}