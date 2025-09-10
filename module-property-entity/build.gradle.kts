
taboolib {
    subproject = false

    description {
        name(project.name.substringAfter('-'))
        desc("Please put this action jar in directory `./plugins/Vulpecula/property/` of your server.")
        dependencies {
            this.dependencies.clear()
            name("DISABLE")
        }
    }
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-core"))
    compileOnly(project(":module-bacikal"))
    compileOnly("ink.ptms.core:v12104:12104:mapped")
}

tasks {
    jar {
        archiveBaseName.set(project.name.substringAfter('-'))
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/property"))
    }
}