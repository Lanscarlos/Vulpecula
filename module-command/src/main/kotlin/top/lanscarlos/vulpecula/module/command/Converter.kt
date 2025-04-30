package top.lanscarlos.vulpecula.module.command

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 11:32
 */
interface Converter<T> {

    fun convert(input: String): T

}