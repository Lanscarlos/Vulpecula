import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.Base64

taboolib {
    subproject = true
}

val projects = rootProject.subprojects
    .filter { it.depth == 1 && it.name.startsWith("plugin-") }
    .flatMap { project ->
        project.configurations["compileOnly"].dependencies
            .filterIsInstance<ProjectDependency>()
            .map { it.dependencyProject }
    }
for (project in projects) {
    project.tasks.register("generateMetadata") {
        dependsOn("classes")
        doLast {
            val workspace = file(project.layout.buildDirectory.dir("resources/main/metadata"))
                .also(File::mkdirs)
            val files = project.sourceSets["main"].output.classesDirs
                .filter { it.exists() }
                .flatMap { it.walk().onEnter { file -> file.name != "META-INF" } }
                .filter { it.isFile && it.extension == "class" }

            // ASM 解析
            for (file in files) {
                val reader = ClassReader(file.readBytes())
                var hasParserAnnotation = false
                val data = mutableListOf<String>()
                reader.accept(object : ClassVisitor(Opcodes.ASM9) {
                    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
                        if (descriptor == "Ltop/lanscarlos/vulpecula/module/bacikal/annotation/BacikalParser;") {
                            hasParserAnnotation = true
                            return null
                        }
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

                if (!hasParserAnnotation) {
                    continue
                }
                val name = reader.className.replace('/', '.') + ".metadata"
                File(workspace, name).writeText(data.joinToString("\n"))
            }
        }
    }
}
