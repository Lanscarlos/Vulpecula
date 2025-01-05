
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-config"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}