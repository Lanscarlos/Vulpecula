package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ClassActionRegisterException(id: String, cause: Throwable) : BacikalException(cause) {

    override val message: String = Lang.BACIKAL_CLASS_ACTION_REGISTER.asText(console(), id, cause.localizedMessage)

}