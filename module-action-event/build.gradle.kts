
version = "1.0.0"

taboolib {
    subproject = false

    description {
        name(project.name.substringAfter('-'))
        desc("Please put this action jar in directory `./plugins/Vulpecula/action/` of your server.")
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
    jar {
        archiveBaseName.set(project.name.substringAfter('-'))
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs/action"))

        dependsOn(":task-generate-metadata:resolve")

        from(layout.buildDirectory.dir("workspace")) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}