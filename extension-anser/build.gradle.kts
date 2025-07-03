import io.izzel.taboolib.gradle.Porticus
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

version = "1.0.0"

taboolib {
    subproject = false
    env {
        install(Porticus)
    }
    description {
        name(project.name.substringAfter('-').uppercaseFirstChar())
        desc("Relays Vulpecula plugin across BungeeCord networks.")
    }
}

tasks {
    jar {
        archiveBaseName.set(project.name.substringAfter('-').uppercaseFirstChar())
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/extension"))
    }
}