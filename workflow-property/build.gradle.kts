
taboolib {
    subproject = true
}

for (project in rootProject.subprojects.filter { it.depth == 1 && it.name.startsWith("module-property-") }) {
    project.tasks.jar {
        archiveClassifier.set("")

        dependsOn("generateMetadata")
    }
}

for (project in rootProject.subprojects.filter { it.depth == 1 && it.name.startsWith("plugin-") }) {
    project.tasks.register("embedProperties") {
        dependsOn("cleanResources")
        val dependencies = project.configurations["compileOnly"].dependencies
            .filterIsInstance<ProjectDependency>()
            .filter { it.name.startsWith("module-property-") }
            .map { it.dependencyProject }
        for (dependency in dependencies) {
            dependsOn(":${dependency.name}:jar")
        }

        doLast {
            val workspace = file(project.layout.buildDirectory.dir("resources/main/property")).also(File::mkdirs)
            for (dependency in dependencies) {
                val archive = file(dependency.tasks.getByName<Jar>("jar").archiveFile)
                archive.copyTo(File(workspace, archive.name))
            }
        }
    }
}
