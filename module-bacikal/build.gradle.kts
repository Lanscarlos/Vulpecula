
taboolib {
    subproject = true
    relocate("kotlin.Metadata", "kotlin2021.Metadata")
    relocate("kotlin.", "kotlin2021.")
    relocate("kotlinx.metadata.", "kotlinx.metadata060.")
}

dependencies {
//    compileOnly(project(":project:common"))
//    compileOnly(project(":project:module-config"))
    compileOnly(project(":module-applicative"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}