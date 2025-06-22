package top.lanscarlos.vulpecula.config

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.applicative.*
import java.io.File
import java.nio.file.Path
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:30
 */
abstract class AbstractDynamicConfig(override val file: File, val config: Configuration) : DynamicConfig, Runnable {

    override val path: Path by lazy {
        getDataFolder().toPath().relativize(file.toPath())
    }

    val sections = linkedMapOf<String, DynamicSection<*>>()

    var onBeforeReload: Runnable? = null
    var onAfterReload: Runnable? = null

    init {
        config.onReload(this)
    }

    override fun run() {
        onBeforeReload?.run()
        sections.values.forEach {
            it.update()
        }
        onAfterReload?.run()
    }

    override fun reload() {
        config.reload()
    }

    override fun onBeforeReload(runnable: Runnable) {
        onBeforeReload = runnable
    }

    override fun onAfterReload(runnable: Runnable) {
        onAfterReload = runnable
    }

    override fun getKeys(deep: Boolean): Set<String> {
        return config.getKeys(deep)
    }

    override fun getKeys(path: String, deep: Boolean): Set<String> {
        return config.getConfigurationSection(path)?.getKeys(deep) ?: emptySet()
    }

    override fun get(path: String): Any? {
        return config[path]
    }

    override fun getString(path: String): String? {
        return config.getString(path)
    }

    override fun getString(path: String, def: String): String {
        return getString(path) ?: def
    }

    override fun <T> read(path: String, transfer: Function<Any?, T>): DynamicSection<T> {
        return DefaultDynamicSection(this, path, transfer).also {
            sections[path] = it
        }
    }

    override fun read(path: String): DynamicSection<Any?> {
        return read(path) { it }
    }

    override fun readBoolean(path: String, def: Boolean): DynamicSection<Boolean> {
        return read(path) { it?.applicativeBoolean() ?: def }
    }

    override fun readInt(path: String, def: Int): DynamicSection<Int> {
        return read(path) { it?.applicativeInt() ?: def }
    }

    override fun readLong(path: String, def: Long): DynamicSection<Long> {
        return read(path) { it?.applicativeLong() ?: def }
    }

    override fun readFloat(path: String, def: Float): DynamicSection<Float> {
        return read(path) { it?.applicativeFloat() ?: def }
    }

    override fun readDouble(path: String, def: Double): DynamicSection<Double> {
        return read(path) { it?.applicativeDouble() ?: def }
    }

    override fun readString(path: String): DynamicSection<String?> {
        return read(path) { it?.toString() }
    }

    override fun readString(path: String, def: String): DynamicSection<String> {
        return read(path) { it?.toString() ?: def }
    }

    override fun readIntList(path: String, def: List<Int>): DynamicSection<List<Int>> {
        return read(path) { it?.applicativeIntList() ?: def }
    }

    override fun readStringList(path: String, def: List<String>): DynamicSection<List<String>> {
        return read(path) { it?.applicativeStringList() ?: def }
    }

}