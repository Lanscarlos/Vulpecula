package top.lanscarlos.vulpecula.config

import taboolib.library.configuration.ConfigurationSection
import kotlin.reflect.KProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 21:36
 */
class VulConfigNodeTransfer<R: Any>(
    val path: String,
    val wrapper: VulConfig,
    private val transfer: ConfigurationSection.(Any?) -> R
) : VulConfigNode<R> {

    lateinit var value: R

    var raw: Any? = null

    override operator fun getValue(any: Any?, property: KProperty<*>): R {
        if (!::value.isInitialized) {
            raw = wrapper.source[path]
            value = transfer(wrapper.source, raw)
        }
        return value
    }

    /**
     * 更新数据源
     * @return 返回数值发生变化的节点名
     * */
    fun update(config: ConfigurationSection): Boolean {
        val newRaw = config[path]
        if (raw == newRaw) return false

        // 前后数值发生变化
        this.value = transfer(config, newRaw)
        return true
    }
}