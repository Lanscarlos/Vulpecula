package top.lanscarlos.vulpecula

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:27
 */
object PluginMetrics {

    private lateinit var metrics: Metrics

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        metrics = Metrics(17183, BukkitPlugin.getInstance().description.version, Platform.BUKKIT)
//        metrics.addCustomChart(SingleLineChart("event-mapping") {
//            EventMapper.cache.size
//        })
//        metrics.addCustomChart(SingleLineChart("scripts-source") {
//            ExternalScript.cache.size
//        })
//        metrics.addCustomChart(SingleLineChart("scripts-compiled") {
//            ScriptWorkspace.scripts.size
//        })
//        metrics.addCustomChart(SingleLineChart("custom-command") {
//            CustomCommand.cache.size
//        })
//        metrics.addCustomChart(SingleLineChart("dispatchers") {
//            EventDispatcher.cache.size
//        })
//        metrics.addCustomChart(SingleLineChart("handlers") {
//            EventHandler.cache.size
//        })
//        metrics.addCustomChart(SingleLineChart("schedules") {
//            ScheduleTask.cache.size
//        })
    }

}