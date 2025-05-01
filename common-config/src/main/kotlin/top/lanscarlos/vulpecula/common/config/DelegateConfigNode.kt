package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.livedata.DefaultLiveData
import top.lanscarlos.vulpecula.common.livedata.LiveData

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 17:06
 */
class DelegateConfigNode(val config: ConfigurationSection, val keys: Array<out String>) : LiveData<Any?> {

    lateinit var key: String

    override val isInitialized: Boolean
        get() = liveData.isInitialized

    val liveData = DefaultLiveData(source = ::read, transformer = ::transformer)

    init {
        // 获取根配置
        var parent: ConfigurationSection = config
        while (parent.parent != null) {
            parent = parent.parent!!
        }

        // 注册变动监听
        val root = parent as? Configuration ?: error("Root configuration not found.")
        root.onReload(::update)
    }

    private fun read(): Any? {
        for (key in keys) {
            if (!config.contains(key)) {
                continue
            }
            this.key = key
            return config[key]
        }
        return null
    }

    private fun transformer(value: Any?): Any? {
        return value
    }

    override fun getValue(): Any? {
        return liveData.getValue()
    }

    override fun getValueOrNull(): Any? {
        return liveData.getValueOrNull()
    }

    override fun update() {
        liveData.update()
    }

}