
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":common-config"))
    compileOnly(project(":common-core"))
    compileOnly("ink.ptms.core:v12004:12004:universal")
}