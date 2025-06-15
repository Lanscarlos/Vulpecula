
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
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module-command"))
    compileOnly(project(":module-core"))
    compileOnly(project(":module-dispatcher"))
    compileOnly(project(":module-schedule"))
    compileOnly(project(":module-script"))
    compileOnly(project(":platform-bukkit"))
}

tasks {
    register<Copy>("embed-action") {
        dependsOn(":module-action-event:jar")
        from(project(":module-action-event").tasks.getByName<Jar>("jar").archiveFile) // 获取 jar
        into(layout.buildDirectory.dir("workspace/action"))
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
                resource.appendText("\n\n\n")
                resource.appendBytes(file.readBytes())
            }
        }

        for (resource in resources) {
            println("resource ${resource.key} >> \n" + resource.value.readText())
        }
    }

    jar {
        archiveBaseName.set("${rootProject.name}-experiment")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))

        dependsOn("embed-action")
        dependsOn("merge-resources")

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

        doLast {
            delete(layout.buildDirectory)
        }
    }
}