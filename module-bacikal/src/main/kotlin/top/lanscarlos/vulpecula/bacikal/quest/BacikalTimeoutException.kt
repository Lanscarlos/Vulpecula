package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.printKetherErrorMessage

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-02 20:04
 */
class BacikalTimeoutException(
    cause: Throwable,
    properties: Map<String, Any>,
    timeout: Long
) : BacikalRuntimeException(cause, properties) {

    override val message: String = "Timeout ${timeout}ms"

    override fun getLocalizedMessage(): String {
        return message
    }

    override fun printKetherMessage(detailError: Boolean) {
        this.printKetherErrorMessage(detailError)
    }

}