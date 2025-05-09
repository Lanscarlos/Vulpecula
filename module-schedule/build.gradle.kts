
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-config"))
    compileOnly(project(":common-livedata"))
    compileOnly(project(":common-message"))
    compileOnly(project(":module-bacikal"))
    compileOnly(project(":module-script"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")

    compileOnly("com.ucasoft.kcron:kcron-common:0.23.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}