
taboolib {
    subproject = false
    relocate("kotlin", "kotlin1822")
}

dependencies {
    compileOnly(project(":project:module-bacikal"))
}