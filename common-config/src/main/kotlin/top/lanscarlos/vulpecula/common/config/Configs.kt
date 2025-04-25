package top.lanscarlos.vulpecula.common.config

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.File
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
     * */
    fun reload() {
        // 重载主配置
        config.reload()

        // 重载所有订阅者
        for (workspace in services) {
            workspace.reload()
        }
    }

    fun register(id: String, directory: File, priority: Int = 8, callback: ConfigServiceCallback) {
        services += ConfigService(id, directory, priority, callback)
        services.sortByDescending { it.priority } // 降序, 确保优先级高的先被重载
    }

    fun unregister(id: String) {
        services.removeIf { it.id == id }
    }

}