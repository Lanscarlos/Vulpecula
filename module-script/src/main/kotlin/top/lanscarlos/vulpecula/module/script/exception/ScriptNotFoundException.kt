package top.lanscarlos.vulpecula.module.script.exception

import top.lanscarlos.vulpecula.common.message.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script.exception
 *
 * @author Lanscarlos
 * @since 2025/5/6 14:20
 */
class ScriptNotFoundException(scriptId: String) : RuntimeException() {

    override val message: String = MessageService.asLang("module-script-exception-script-not-found", scriptId)

}