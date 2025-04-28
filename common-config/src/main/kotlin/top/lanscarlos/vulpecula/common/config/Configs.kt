package top.lanscarlos.vulpecula.common.config

import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 09:51
 */
object Configs {

    @Config("config.yml")
    lateinit var config: Configuration
        private set

    val services = LinkedList<ConfigService>()

    /**
     * 重载所有配置
     *
     * @return 本次加载所涉及的调试信息
     * */
    fun reload(): List<String> {
        val logs = LinkedList<String>()

        // 调试计时
        val startTime = System.nanoTime()

        // 重载主配置
        config.reload()
        // 计算耗时, 单位毫秒
        val time = Coerce.format((System.nanoTime() - startTime).div(1000000.0))
        logs += console().asLangText("common-config-main-load-succeeded", time)

        // 重载所有服务
        for (service in services) {
            logs += service.load()
        }
        return logs
    }

    fun register(service: ConfigService) {
        require(services.all { it.id != service.id }) { "Duplicate service ${service.id}." }
        services += service
        services.sortByDescending { it.priority } // 降序, 确保优先级高的先被重载
    }

    fun unregister(id: String) {
        services.removeIf { it.id == id }
    }

}