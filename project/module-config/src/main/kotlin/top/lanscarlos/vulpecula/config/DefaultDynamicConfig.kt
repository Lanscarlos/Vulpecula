package top.lanscarlos.vulpecula.config

import taboolib.module.configuration.ConfigLoader
import taboolib.module.configuration.ConfigNodeFile
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.applicative.CollectionApplicative.Companion.collection
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeBoolean
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeDouble
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeFloat
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeInt
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeLong
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeShort
import top.lanscarlos.vulpecula.applicative.StringListApplicative.Companion.applicativeStringList
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:30
 */
class DefaultDynamicConfig(val bind: String) : DynamicConfig, Runnable {

    val config: ConfigNodeFile by lazy {
        ConfigLoader.files[bind] ?: error("Config $bind not found.")
    }

    val configuration: Configuration
        get() = config.configuration

    val sections = linkedMapOf<String, DynamicSection<*>>()

    init {
        configuration.onReload(this)
    }

    override fun run() {
        sections.values.forEach {
            it.update()
        }
    }

    override fun get(path: String): Any? {
        return configuration[path]
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
        return read(path) { it?.applicativeBoolean()?.getValue() ?: def }
    }

    override fun readShort(path: String, def: Short): DynamicSection<Short> {
        return read(path) { it?.applicativeShort()?.getValue() ?: def }
    }

    override fun readInt(path: String, def: Int): DynamicSection<Int> {
        return read(path) { it?.applicativeInt()?.getValue() ?: def }
    }

    override fun readLong(path: String, def: Long): DynamicSection<Long> {
        return read(path) { it?.applicativeLong()?.getValue() ?: def }
    }

    override fun readFloat(path: String, def: Float): DynamicSection<Float> {
        return read(path) { it?.applicativeFloat()?.getValue() ?: def }
    }

    override fun readDouble(path: String, def: Double): DynamicSection<Double> {
        return read(path) { it?.applicativeDouble()?.getValue() ?: def }
    }

    override fun readString(path: String, def: String): DynamicSection<String> {
        return read(path) { it?.toString() ?: def }
    }

    override fun readIntList(path: String, def: List<Int>): DynamicSection<List<Int>> {
        return read(path) { it?.applicativeInt()?.collection()?.getValue()?.toList() ?: def }
    }

    override fun readStringList(path: String, def: List<String>): DynamicSection<List<String>> {
        return read(path) { it?.applicativeStringList()?.getValue() ?: def }
    }

}