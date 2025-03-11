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

    val subscribers = LinkedList<ConfigSubscriber>()

    /**
     * 重载所有配置
     * */
    fun reload() {
        // 重载主配置
        config.reload()

        // 重载所有订阅者
        for (subscriber in subscribers) {
            subscriber.reload()
        }
    }

    fun subscribe(file: File, priority: Int = 8, callback: ConfigSubscriber.Callback) {
        subscribers += ConfigSubscriber(file, priority, callback)
        subscribers.sortByDescending { it.priority } // 降序, 确保优先级高的先被重载
    }

    fun unsubscribe(file: File) {
        subscribers.removeIf { it.file == file }
    }

}