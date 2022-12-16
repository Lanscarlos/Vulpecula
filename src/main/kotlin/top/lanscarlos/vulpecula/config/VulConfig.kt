package top.lanscarlos.vulpecula.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 19:16
 */
class VulConfig(
    config: ConfigurationSection
) {

    var source: ConfigurationSection = config
        private set

    var root: Configuration? = root()
        private set

    val nodes = CopyOnWriteArraySet<VulConfigNode<*>>()

    fun <T> read(path: String, transfer: ConfigurationSection.(Any?) -> T): VulConfigNode<T> {
        return VulConfigNodeTransfer(path, this, transfer).also {
            nodes += it
        }
    }

    fun readBoolean(path: String, def: Boolean = false): VulConfigNode<Boolean> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceBoolean(def)
        }
    }

    fun readShort(path: String, def: Short = 0): VulConfigNode<Short> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceShort(def)
        }
    }

    fun readInt(path: String, def: Int = 0): VulConfigNode<Int> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceInt(def)
        }
    }

    fun readLong(path: String, def: Long = 0): VulConfigNode<Long> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceLong(def)
        }
    }

    fun readFloat(path: String, def: Float = 0f): VulConfigNode<Float> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceFloat(def)
        }
    }

    fun readDouble(path: String, def: Double = 0.0): VulConfigNode<Double> {
        return VulConfigNodeTransfer(path, this) {
            it.coerceDouble(def)
        }
    }

    fun readString(path: String, def: String = ""): VulConfigNode<String> {
        return VulConfigNodeTransfer(path, this) {
            it?.toString() ?: def
        }
    }

    fun readIntList(path: String, def: List<Int> = emptyList()): VulConfigNode<List<Int>> {
        return VulConfigNodeTransfer(path, this) { value ->
            when (value) {
                is Int -> listOf(value)
                is String -> listOf(value.coerceInt())
                is Array<*> -> value.mapNotNull { it?.coerceInt() }
                is Collection<*> -> value.mapNotNull { it?.coerceInt() }
                else -> def
            }
        }
    }

    fun readStringList(path: String, def: List<String> = emptyList()): VulConfigNode<List<String>> {
        return VulConfigNodeTransfer(path, this) { value ->
            when (value) {
                is String -> listOf(value)
                is Array<*> -> value.mapNotNull { it?.toString() }
                is Collection<*> -> value.mapNotNull { it?.toString() }
                else -> def
            }
        }
    }

    fun updateSource(config: ConfigurationSection): List<Pair<*, *>> {
        this.source = config
        this.root = root()
        return nodes.mapNotNull {
            if (it !is VulConfigNodeTransfer<*>) return@mapNotNull null
            it.update(config)?.let { value -> it.path to value }
        }
    }

    private fun root(): Configuration? {
        var parent = source
        while (parent.parent != null) {
            if (parent is Configuration) break
            parent = parent.parent!!
        }
        return parent as? Configuration
    }
}