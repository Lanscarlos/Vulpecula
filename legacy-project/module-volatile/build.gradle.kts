
taboolib {
    subproject = true
}

dependencies {
    compileOnly(project(":project:common"))

    compileOnly("ink.ptms.core:v12000:12000:mapped")
    compileOnly("ink.ptms:nms-all:1.0.0")
}