
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-config"))
    compileOnly(project(":common-core"))
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module-script"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-20241030.192207-176")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks.test {
    useJUnitPlatform()
}