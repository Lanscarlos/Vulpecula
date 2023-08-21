package top.lanscarlos.vulpecula.config

import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.config
 *
 * @author Lanscarlos
 * @since 2023-08-21 21:34
 */
interface DynamicConfig {



    fun read(path: String): DynamicSection<Any?> {
        return read(path) { it }
    }

    fun <T> read(path: String, func: Function<Any?, T>): DynamicSection<T>

}