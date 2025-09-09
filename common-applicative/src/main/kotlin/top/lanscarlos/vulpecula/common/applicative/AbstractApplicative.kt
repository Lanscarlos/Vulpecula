package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.platform.function.warning
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import top.lanscarlos.vulpecula.common.applicative.exception.NullValueException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 13:57
 */
abstract class AbstractApplicative<T>(val clazz: Class<T>) : Applicative<T> {

    override val name: String = clazz.simpleName.lowercase()

    override val aliases: Array<String> = emptyArray()

    abstract fun convertOrThrow(instance: Any): T

    override fun convert(instance: Any?): T {
        if (instance == null) {
            throw NullValueException()
        }
        return convertOrThrow(instance)
    }

    override fun convertOrNull(instance: Any?): T? {
        if (instance == null) {
            return null
        }
        return try {
            convertOrThrow(instance)
        } catch (e: Exception) {
            null
        }
    }

}
