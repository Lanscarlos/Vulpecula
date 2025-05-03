package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.module.kether.printKetherErrorMessage

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:54
 */
open class BacikalRuntimeException(
    cause: Throwable,
    properties: Map<String, Any>
) : BacikalException(cause) {

    override val message: String = cause.message ?: "EXCEPTION_MESSAGE_MISSING"

    val header = properties["bacikal-header"].toString()

    val location = properties["bacikal-content"].toString()

    open fun printKetherMessage(detailError: Boolean = false) {
        cause.printKetherErrorMessage(detailError)
    }

}