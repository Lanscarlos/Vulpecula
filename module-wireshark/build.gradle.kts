
taboolib {
    subproject = true
}

dependencies {
    compileOnly("ink.ptms.core:v12001:12001:mapped")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
