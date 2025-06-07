
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-config"))
    compileOnly(project(":common-core"))
    compileOnly(project(":common-lang"))
    compileOnly(project(":module-bacikal"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")

    compileOnly("io.foldright:cffu:1.1.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}