package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.library.kether.Quest
import taboolib.module.kether.printKetherErrorMessage

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-02 20:04
 */
class BacikalTimeoutException(
    cause: Throwable,
    quest: Quest,
    properties: Map<String, Any>,
    timeout: Long
) : BacikalRuntimeException(cause, quest, properties) {

    override val message: String = "Timeout ${timeout}ms"

    override fun getLocalizedMessage(): String {
        return message
    }

    override fun printKetherMessage(detailError: Boolean) {
        this.printKetherErrorMessage(detailError)
    }

}