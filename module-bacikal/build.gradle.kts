
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-core"))
    compileOnly(project(":common-diagram"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}