package top.lanscarlos.vulpecula

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

object Vulpecula : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    @Config("config.yml")
    lateinit var config: Configuration
        private set

    override fun onEnable() {
        VulpeculaContext.load(config)
        info("Successfully running ExamplePlugin!")
    }
}