import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

taboolib {
    subproject = true
}

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-command"))
    implementation(project(":project:module-applicative"))
    implementation(project(":project:module-bacikal"))
    implementation(project(":project:module-config"))
    implementation(project(":project:module-volatile"))
    implementation(project(":project:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("Vulpecula-Mini")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
        append("config.yml")
        append("lang/zh_CN.yml")
        append("kether.yml")
    }
    build {
        dependsOn(shadowJar)
    }
}