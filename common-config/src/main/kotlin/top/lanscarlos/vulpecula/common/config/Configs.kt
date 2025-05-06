package top.lanscarlos.vulpecula.common.config

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.message.errorSync
import top.lanscarlos.vulpecula.common.message.infoSync
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

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        load(console())
    }

    /**
     * 重载所有配置
     *
     * @param sender 操作者
     * */
    fun load(sender: ProxyCommandSender) {
        // 加载主配置
        try {
            // 调试计时
            val startTime = System.nanoTime()
            config.reload()
            // 计算耗时, 单位毫秒
            val time = Coerce.format((System.nanoTime() - startTime).div(1000000.0))
            sender.infoSync("common-config-main-load-succeeded", time)
        } catch (ex: Exception) {
            sender.errorSync("common-config-main-load-failed", ex.localizedMessage)
        }

        // 重载所有服务
        for (service in services) {
            service.load(sender)
        }
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