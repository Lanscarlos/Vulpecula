
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-applicative"))
    compileOnly(project(":common-core"))
    compileOnly(project(":module-bacikal"))
    compileOnly("ink.ptms.core:v12104:12104:mapped")
}
