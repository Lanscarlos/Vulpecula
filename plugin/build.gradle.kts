import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import shadow.org.apache.tools.zip.ZipEntry
import shadow.org.apache.tools.zip.ZipOutputStream

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-core"))
    implementation(project(":project:module-applicative"))
    implementation(project(":project:module-bacikal"))
    implementation(project(":project:module-volatile"))
    implementation(project(":project:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("Vulpecula")
        archiveClassifier.set("")
        destinationDirectory.set(file("${rootDir}/build/libs"))
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        // taboolib
        relocate("taboolib", "top.lanscarlos.vulpecula.taboolib")
        // kotlin
        relocate("kotlin.", "kotlin1531.") {
            exclude("kotlin.Metadata")
        }

        // merge config
        transform(ConfigMergeTransformer::class.java)
    }
    kotlinSourcesJar {
        // include subprojects
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
    build {
        dependsOn(shadowJar)
    }
}

//publishing {
//    repositories {
//        maven {
//            url = uri("https://repo.tabooproject.org/repository/releases")
//            credentials {
//                username = project.findProperty("taboolibUsername").toString()
//                password = project.findProperty("taboolibPassword").toString()
//            }
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
//        }
//    }
//    publications {
//        create<MavenPublication>("library") {
//            from(components["java"])
//            groupId = project.group.toString()
//        }
//    }
//}

class ConfigMergeTransformer : Transformer {

    private val data = StringBuilder()

    override fun getName(): String {
        return "ConfigMergeTransformer"
    }

    override fun canTransformResource(element: FileTreeElement?): Boolean {
        return element?.name == "config.yml"
    }

    override fun transform(context: TransformerContext?) {
        context?.`is`?.reader()?.readText()?.let {
            data.append(it)
            data.append("\n\n")
        }
    }

    override fun hasTransformedResource(): Boolean {
        return data.isNotEmpty()
    }

    override fun modifyOutputStream(output: ZipOutputStream?, b: Boolean) {
        val newEntry = ZipEntry("config.yml")
        output?.putNextEntry(newEntry)
        output?.writer()?.use {
            it.write(data.toString())
        }
    }
}