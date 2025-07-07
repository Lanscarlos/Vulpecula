
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
    compileOnly(project(":module-action-entity"))
    compileOnly(project(":module-action-event"))
    compileOnly(project(":module-action-illusion"))
    compileOnly(project(":module-action-item"))
    compileOnly(project(":module-action-memory"))
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