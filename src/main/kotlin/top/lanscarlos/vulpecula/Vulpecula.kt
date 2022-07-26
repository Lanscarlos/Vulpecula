package top.lanscarlos.vulpecula

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object Vulpecula : Plugin() {
    override fun onEnable() {
        info("Successfully running ExamplePlugin!")
    }
}