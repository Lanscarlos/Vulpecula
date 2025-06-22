
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-applicative"))
    compileOnly(project(":project:module-bacikal"))
    compileOnly(project(":project:module-config"))

    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v12001:12001:universal")
}