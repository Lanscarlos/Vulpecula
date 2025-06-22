
taboolib {
    subproject = true
}

for (project in rootProject.subprojects.filter { it.depth == 1 && it.name.startsWith("plugin-") }) {
    project.tasks.register("mergeResources") {
        val dependencies = project.configurations["compileOnly"].dependencies
            .filterIsInstance<ProjectDependency>()
            .filter { !it.name.startsWith("module-action-") }
            .map { it.dependencyProject }
        for (dependency in dependencies) {
            dependsOn(":${dependency.name}:processResources")
        }

        doLast {
            val workspace = file(project.layout.buildDirectory.dir("workspace")).also(File::mkdirs)
            val resources = mutableMapOf<String, File>()
            for (dependency in dependencies) {
                for (file in files(dependency.sourceSets["main"].resources)) {
                    val name = file.absolutePath.substringAfter("resources\\")
                    val resource = resources.computeIfAbsent(name) { File(workspace, name) }
                    if (!resource.exists()) {
                        resource.parentFile.mkdirs()
                    } else {
                        resource.appendText("\n\n")
                    }
                    resource.appendBytes(file.readBytes())
                }
            }
        }
    }
}
