package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 17:06
 */
class DelegateConfigNode(val config: ConfigurationSection, private val keys: Array<out String>) : LiveData<Any?>, Runnable {

    override lateinit var id: String

    override var isInitialized = false

    private val root: Configuration

    /**
     * 缓存值
     * */
    private var value: Any? = null

    init {
        // 获取根配置
        var parent: ConfigurationSection = config
        while (parent.parent != null) {
            parent = parent.parent!!
        }

        // 注册变动监听
        root = parent as? Configuration ?: error("Root configuration not found.")
        root.onReload(this)
    }

    override fun run() {
        update()
    }

    private fun read(): Any? {
        for (key in keys) {
            if (!config.contains(key)) {
                continue
            }
            this.id = key
            return config[key]
        }
        return null
    }

    override fun getValue(): Any? {
        if (!isInitialized) {
            // 初始化
            value = read()
            isInitialized = true
        }
        return value
    }

    override fun update() {
        isInitialized = false
    }

    fun dispose() {
        // 移除监听器
        val reloadCallback = root.getProperty<ArrayList<Runnable>>("reloadCallback")
            ?: error("ReloadCallback does not exist.")
        reloadCallback.removeIf { it == this }
    }

}