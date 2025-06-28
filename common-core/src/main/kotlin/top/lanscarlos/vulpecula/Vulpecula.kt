package top.lanscarlos.vulpecula

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.pluginVersion
import taboolib.module.metrics.CustomChart
import taboolib.module.metrics.Metrics
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:07
 */
object Vulpecula {

    private lateinit var metrics: Metrics
    private val metricsCharts: LinkedList<CustomChart> = LinkedList()

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        metrics = Metrics(17183, pluginVersion, Platform.BUKKIT)
        for (chart in metricsCharts) {
            metrics.addCustomChart(chart)
        }
    }

    fun addMetricsChart(chart: CustomChart) {
        if (::metrics.isInitialized.not()) {
            metricsCharts.add(chart)
            return
        }
        metrics.addCustomChart(chart)
    }

}