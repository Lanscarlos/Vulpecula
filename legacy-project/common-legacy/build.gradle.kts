
taboolib {
    subproject = true
}

//plugins {
//    id("io.izzel.taboolib") version "1.60"
//}

//taboolib {
//    description {
//        contributors {
//            name("Lanscarlos")
//        }
//        desc("A Kether Script Extension System for TabooLib")
//        dependencies {
//            name("Adyeshach").optional(true)
//            name("Chemdah").optional(true)
//            name("DungeonPlus").optional(true)
//            name("Planners").optional(true)
//            name("Invero").optional(true)
//            name("Zaphkiel").optional(true)
//
//            name("PlaceholderAPI").optional(true)
//            name("LuckPerms").optional(true)
//        }
//    }
//
////    install("common")
////    install("common-5")
////    install("module-chat")
////    install("module-configuration")
////    install("module-database")
////    install("module-effect")
////    install("module-kether")
////    install("module-lang")
////    install("module-metrics")
////    install("module-nms")
////    install("module-nms-util")
////    install("expansion-command-helper")
////    install("expansion-javascript")
////    install("platform-bukkit")
////    classifier = null
////    version = taboolib_version
////    options("skip-minimize", "keep-kotlin-module", "skip-taboolib-relocate")
//}

dependencies {
    compileOnly(project(":project:module-volatile"))

    compileOnly("ink.ptms.core:v12000:12000:mapped")
    compileOnly("ink.ptms.core:v12000:12000:universal")

    // server
    compileOnly("ink.ptms:nms-all:1.0.0")

    compileOnly("com.google.guava:guava:31.1-jre")

    // for kether
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") // 协程
    compileOnly("com.mojang:datafixerupper:4.0.26")
    compileOnly("net.luckperms:api:5.4")

    // other
    compileOnly(fileTree("libs"))
}