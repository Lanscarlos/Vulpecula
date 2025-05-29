
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-config"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
}