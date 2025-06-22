
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
    compileOnly(project(":common-diagram"))
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

    jar {
        archiveBaseName.set("${rootProject.name}-experiment")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))

        dependsOn("clean-workspace")
        dependsOn(":task-generate-metadata:resolve")
        dependsOn("embedActions")
        dependsOn("mergeResources")

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