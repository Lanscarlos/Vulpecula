package top.lanscarlos.vulpecula.platform.bukkit

import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.platform.bukkit
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:17
 */
@RuntimeDependency(
    "!org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4",
    test = "kotlinx.coroutines.Dispatchers",
    relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
)
object VulpeculaPlugin : Plugin() {

    override fun onEnable() {
        info("Successfully running Vulpecula!")
    }

}