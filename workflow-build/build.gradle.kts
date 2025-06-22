taboolib {
    subproject = true
}

for (project in rootProject.subprojects.filter { it.depth == 1 && it.name.startsWith("plugin-") }) {
    project.tasks.register("cleanResources") {
        delete(project.layout.buildDirectory.dir("resources"))
        val workspace = file(project.layout.buildDirectory.dir("resources/main/action")).also(File::mkdirs)
    }
    project.tasks.jar {
        archiveClassifier.set("")

        val dependencies = project.configurations["compileOnly"].dependencies
            .filterIsInstance<ProjectDependency>()
            .map { it.dependencyProject }
        for (dependency in dependencies) {
            dependsOn(":${dependency.name}:generateMetadata")
        }
        dependsOn("embedActions")
        dependsOn("mergeResources")

        // 打包资源文件
//        from(layout.buildDirectory.dir("workspace")) {
//            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        }

        // 打包子项目源码
        for (dependency in dependencies) {
            if (dependency.name.startsWith("module-action-")) {
                // 排除拓展语句
                continue
            }
            from(dependency.sourceSets["main"].output) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }
    }
}