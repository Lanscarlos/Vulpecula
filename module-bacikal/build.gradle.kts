
taboolib {
    subproject = true
}

dependencies {
//    compileOnly(project(":project:common"))
//    compileOnly(project(":project:module-config"))
    compileOnly(project(":module-applicative"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}