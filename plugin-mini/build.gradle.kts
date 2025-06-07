
taboolib {
    subproject = false
    relocate("kotlinx.metadata.", "kotlinx.metadata060.")
    relocate("com.ucasoft.kcron.", "com.ucasoft.kcron0230.")
    relocate("io.foldright.cffu.", "io.foldright.cffu113.")
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.name}-mini")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))

        val subprojects = listOf(
            project(":common-applicative"),
            project(":common-config"),
            project(":common-core"),
            project(":common-lang"),
            project(":module-bacikal"),
            project(":module-command"),
            project(":module-core"),
            project(":module-schedule"),
            project(":module-script"),
            project(":platform-bukkit")
        )

        // 打包并合并子项目资源
        val workspace = File(buildDir, "workspace")
        val resources = files(*subprojects.map { it.sourceSets["main"].resources }.toTypedArray())
        val artifacts = mutableMapOf<String, File>()
        for (file in resources) {
            val name = file.absolutePath.substringAfter("resources\\")
            val artifact = artifacts.computeIfAbsent(name) { File(workspace, name) }
            if (!artifact.parentFile.exists()) {
                artifact.parentFile.mkdirs()
            }
            artifact.appendBytes(file.readBytes())
        }
        from(workspace) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        // 打包子项目源码
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].output) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }
    }
}