
taboolib {
    subproject = false
}

dependencies {
    compileOnly(project(":project:module-bacikal"))
}

tasks {
    jar {
        archiveBaseName.set("action-${project.name}")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs/actions"))
    }
}