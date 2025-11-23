package top.lanscarlos.vulpecula.common.config

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.TimeUtil
import top.lanscarlos.vulpecula.common.utils.asLang
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

    val name: String get() = asLang("common-core-message-module")

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
            val startTime = TimeUtil.startTiming()
            config.reload()
            // 计算耗时, 单位毫秒
            Lang.COMMON_CONFIG_MAIN_LOAD_SUCCESS.info(sender, TimeUtil.stopTiming(startTime))
        } catch (ex: Exception) {
            Lang.COMMON_CONFIG_MAIN_LOAD_FAILURE.error(sender, ex.localizedMessage)
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