package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.console
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:54
 */
class BacikalRuntimeException(
    val action: ParsedAction<*>,
    properties: Map<String, Any>,
    val native: Throwable
) : RuntimeException() {

    override val cause: Throwable = native

    override val message: String = native.message ?: "EXCEPTION_MESSAGE_MISSING"

    val actionName = properties["bacikal-token"].toString()

    val actionDetails = properties["bacikal-content"].toString()

    override fun getLocalizedMessage(): String {
        return console().asLangText("module-bacikal-runtime-exception-message", actionName, message, actionDetails)
    }

    fun printKetherErrorMessage(detailError: Boolean = false) {
        native.printKetherErrorMessage(detailError)
    }

}