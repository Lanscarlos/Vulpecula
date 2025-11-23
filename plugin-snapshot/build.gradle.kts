
taboolib {
    subproject = false
    relocate("top.lanscarlos.module.", "top.lanscarlos.vulpecula.module.")
    relocate("kotlinx.metadata.", "kotlinx.metadata060.")
    relocate("com.ucasoft.kcron.", "com.ucasoft.kcron0230.")
    relocate("io.foldright.cffu.", "io.foldright.cffu113.")
}

dependencies {
    compileOnly(project(":common-core"))
    compileOnly(project(":common-diagram"))
    compileOnly(project(":extension-action-entity"))
    compileOnly(project(":extension-action-event"))
    compileOnly(project(":extension-action-illusion"))
    compileOnly(project(":extension-action-item"))
    compileOnly(project(":extension-action-memory"))
    compileOnly(project(":extension-action-target"))
    compileOnly(project(":extension-property-common"))
    compileOnly(project(":extension-property-entity"))
    compileOnly(project(":module-anser"))
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module-command"))
    compileOnly(project(":module-core"))
    compileOnly(project(":module-dispatcher"))
    compileOnly(project(":module-schedule"))
    compileOnly(project(":module-script"))
    compileOnly(project(":module-volatility"))
    compileOnly(project(":platform-bukkit"))
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.name}-${project.name.substringAfter('-')}")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}