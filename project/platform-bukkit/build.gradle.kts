val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.56"
}

taboolib {
    description {
        name = rootProject.name
        contributors {
            name("Lanscarlos")
        }
        desc("A Kether Multifaceted Extension System for TabooLib.")
        dependencies {
            name("Adyeshach").optional(true)
            name("Chemdah").optional(true)
            name("DungeonPlus").optional(true)
            name("Planners").optional(true)
            name("Invero").optional(true)
            name("Zaphkiel").optional(true)

            name("PlaceholderAPI").optional(true)
            name("LuckPerms").optional(true)
        }
    }

    install("common", "platform-bukkit")
    options("skip-minimize", "keep-kotlin-module", "skip-taboolib-relocate")
    classifier = null
    version = taboolib_version
}