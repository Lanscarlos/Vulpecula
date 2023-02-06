package top.lanscarlos.vulpecula

import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.metrics.Metrics
import taboolib.module.metrics.charts.SingleLineChart
import taboolib.platform.BukkitPlugin
import top.lanscarlos.vulpecula.internal.CustomCommand
import top.lanscarlos.vulpecula.internal.EventDispatcher
import top.lanscarlos.vulpecula.internal.EventHandler
import top.lanscarlos.vulpecula.internal.EventMapper
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.script.VulScript
import top.lanscarlos.vulpecula.script.ScriptWorkspace

object Vulpecula : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    lateinit var metrics: Metrics
        private set

    override fun onEnable() {
        VulpeculaContext.load(true)
        info("Successfully running Vulpecula!")
    }

    override fun onActive() {
        metrics = Metrics(17183, plugin.description.version, Platform.BUKKIT)
        metrics.addCustomChart(SingleLineChart("event-mapping") {
            EventMapper.cache.size
        })
        metrics.addCustomChart(SingleLineChart("scripts-source") {
            VulScript.cache.size
        })
        metrics.addCustomChart(SingleLineChart("scripts-compiled") {
            ScriptWorkspace.scripts.size
        })
        metrics.addCustomChart(SingleLineChart("custom-command") {
            CustomCommand.cache.size
        })
        metrics.addCustomChart(SingleLineChart("dispatchers") {
            EventDispatcher.cache.size
        })
        metrics.addCustomChart(SingleLineChart("handlers") {
            EventHandler.cache.size
        })
        metrics.addCustomChart(SingleLineChart("schedules") {
            ScheduleTask.cache.size
        })
    }
}