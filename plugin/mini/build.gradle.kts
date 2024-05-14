
taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("Lanscarlos")
        }
        dependencies {
            name("Adyeshach").optional(true)
            name("Chemdah").optional(true)
            name("DungeonPlus").optional(true)
            name("Planners").optional(true)
            name("Invero").optional(true)
            name("Zaphkiel").optional(true)

            name("PlaceholderAPI").optional(true)
            name("LuckPerms").optional(true)
        }
    }
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.name}-Mini")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))

        val subprojects = listOf(
            project(":project:common"),
            project(":project:common-core"),
            project(":project:module-applicative"),
            project(":project:module-bacikal"),
            project(":project:module-config"),
            project(":project:module-volatile"),
            project(":project:platform-bukkit")
        )

        // 打包子项目源码
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].java)
            from(subproject.sourceSets["main"].kotlin)
        }

        // 打包并合并子项目资源
        val workspace = File(buildDir, "workspace")
        if (workspace.exists()) {
            workspace.deleteRecursively()
        }
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
        from(workspace)
    }
}