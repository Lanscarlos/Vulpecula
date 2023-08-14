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

@Suppress("UNCHECKED_CAST")
class DynamicConfigNodeTransfer<R>(
    val path: String,
    val wrapper: DynamicConfig,
    private val transfer: ConfigurationSection.(Any?) -> R
) : DynamicConfigNode<R> {

    var isInitialized = false
    var raw: Any? = null
    var value: R? = null

    override operator fun getValue(any: Any?, property: KProperty<*>): R {
        if (!isInitialized) {
            raw = wrapper.source[path]
            value = transfer(wrapper.source, raw)
            isInitialized = true
        }
        return value as R
    }

    /**
     * 更新数据源
     * @return 若数据发生变化则返回旧值与新值，反之 null
     * */
    fun update(config: ConfigurationSection): Pair<R, R>? {
        val newRaw = config[path]
        if (raw == newRaw) return null

        // 更新原始数据
        raw = newRaw

        // 前后数值发生变化
        val oldValue = this.value as R
        val newValue = transfer(config, newRaw)
        this.value = newValue
        return oldValue to newValue
    }
}