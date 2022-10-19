package top.lanscarlos.vulpecula

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.platform.BukkitPlugin

object Vulpecula : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    override fun onEnable() {
        VulpeculaContext.load(false)
        info("Successfully running ExamplePlugin!")
    }
}