
version = "1.0.0"

taboolib {
    subproject = false

    description {
        name("Vulpecula-Extension-Action-Item")
        desc("Please put this extension in directory `./plugins/Vulpecula/extension/` of your server.")
        contributors {
            this.contributors.clear()
            name("Lanscarlos")
        }
        dependencies {
            this.dependencies.clear()
            name("DISABLE")
        }
    }

    relocate("lanscarlos", "aiurlanta")
}

dependencies {
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module:item"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
}

tasks {
    jar {
        archiveBaseName.set(project.name.replace("module", "extension"))
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
    }
}