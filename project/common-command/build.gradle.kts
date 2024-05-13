
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":project:module-applicative"))
    compileOnly(project(":project:module-config"))
    compileOnly(project(":project:module-bacikal"))

    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v12001:12001:universal")
}