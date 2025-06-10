package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 17:06
 */
class DelegateConfigNode(val config: ConfigurationSection, private val keys: Array<out String>) : LiveData<Any?>, Runnable {

    override var id: String = keys.first()

    private var onUpdate: Consumer<Any?>? = null

    private var value: Any? = null

    init {
        // 初始化并读取值
        run()

        if (config is Configuration) {
            // 注册变动监听
            config.onReload(this)
        }
    }

    override fun run() {
        value = read()
        onUpdate?.accept(getValue())
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
        return value
    }

    override fun onUpdate(func: Consumer<Any?>) {
        onUpdate = func
    }

    fun dispose() {
        // 移除监听器
        if (config !is Configuration) {
            return
        }
        val reloadCallback = config.getProperty<ArrayList<Runnable>>("reloadCallback")
            ?: error("DelegateConfigNode#dispose >> ReloadCallback does not exist.")
        reloadCallback.removeIf { it == this }
    }

}