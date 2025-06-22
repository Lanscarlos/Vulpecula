package top.lanscarlos.vulpecula.legacy.config

import taboolib.common5.*
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2022-12-15 19:16
 */
class DynamicConfig(
    config: ConfigurationSection
) {

    var source: ConfigurationSection = config
        private set

    var root: Configuration? = root()
        private set

    val nodes = CopyOnWriteArraySet<DynamicConfigNode<*>>()

    fun updateSource(config: ConfigurationSection): List<Pair<String, Pair<Any?, Any?>>> {
        this.source = config
        this.root = root()
        return nodes.mapNotNull {
            if (it !is DynamicConfigNodeTransfer<*>) return@mapNotNull null
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

    fun read(path: String): DynamicConfigNode<Any?> {
        return DynamicConfigNodeTransfer(path, this, transfer = { it }).also {
            nodes += it
        }
    }

    fun <T> read(path: String, transfer: ConfigurationSection.(Any?) -> T): DynamicConfigNode<T> {
        return DynamicConfigNodeTransfer(path, this, transfer).also {
            nodes += it
        }
    }

    fun readBoolean(path: String, def: Boolean = false): DynamicConfigNode<Boolean> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.cbool ?: def
        }.also {
            nodes += it
        }
    }

    fun readShort(path: String, def: Short = 0): DynamicConfigNode<Short> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.cshort ?: def
        }.also {
            nodes += it
        }
    }

    fun readInt(path: String, def: Int = 0): DynamicConfigNode<Int> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.cint ?: def
        }.also {
            nodes += it
        }
    }

    fun readLong(path: String, def: Long = 0): DynamicConfigNode<Long> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.clong ?: def
        }.also {
            nodes += it
        }
    }

    fun readFloat(path: String, def: Float = 0f): DynamicConfigNode<Float> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.cfloat ?: def
        }.also {
            nodes += it
        }
    }

    fun readDouble(path: String, def: Double = 0.0): DynamicConfigNode<Double> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.cdouble ?: def
        }.also {
            nodes += it
        }
    }

    fun readString(path: String): DynamicConfigNode<String?> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.toString()
        }.also {
            nodes += it
        }
    }

    fun readString(path: String, def: String): DynamicConfigNode<String> {
        return DynamicConfigNodeTransfer(path, this) {
            it?.toString() ?: def
        }.also {
            nodes += it
        }
    }

    fun readIntList(path: String, def: List<Int> = emptyList()): DynamicConfigNode<List<Int>> {
        return DynamicConfigNodeTransfer(path, this) { value ->
            when (value) {
                is Int -> listOf(value)
                is String -> listOf(value.cint)
                is Array<*> -> value.mapNotNull { it?.cint }
                is Collection<*> -> value.mapNotNull { it?.cint }
                else -> def
            }
        }.also {
            nodes += it
        }
    }

    fun readStringList(path: String, def: List<String> = emptyList()): DynamicConfigNode<List<String>> {
        return DynamicConfigNodeTransfer(path, this) { value ->
            when (value) {
                is String -> listOf(value)
                is Array<*> -> value.mapNotNull { it?.toString() }
                is Collection<*> -> value.mapNotNull { it?.toString() }
                else -> def
            }
        }.also {
            nodes += it
        }
    }

    companion object {

        fun ConfigurationSection.toDynamic(): DynamicConfig = DynamicConfig(this)

        fun <T> bindConfigNode(path: String, bind: String = "config.yml", func: ConfigurationSection.(Any?) -> T): DynamicConfigNode<T> {
            return DynamicConfigNodeBinding(path, bind, func)
        }
    }
}