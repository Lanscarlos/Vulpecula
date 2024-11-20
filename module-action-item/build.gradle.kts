
version = "1.0.0"

taboolib {
    subproject = false
}

dependencies {
    compileOnly(project(":module-bacikal"))
}

tasks {
    jar {
        archiveBaseName.set(project.name.replace("module", "extension"))
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
    }
}