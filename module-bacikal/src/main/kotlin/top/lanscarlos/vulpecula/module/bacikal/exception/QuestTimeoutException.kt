package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.module.lang.sendErrorMessage
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-02 20:04
 */
class QuestTimeoutException(
    cause: Throwable,
    quest: Quest,
    properties: Map<String, Any>,
    timeout: Long
) : QuestRuntimeException(cause, quest, properties) {

    override val message: String = asLang("module-bacikal-exception-execute-timeout", timeout)

    override fun getLocalizedMessage(): String {
        return message
    }

    override fun notice(receiver: ProxyCommandSender) {
        Lang.EXCEPTION_QUEST_ACTION.error(receiver, content)
        Lang.EXCEPTION_QUEST_REASON.error(receiver, message)
        receiver.sendErrorMessage(getDetailMessage(receiver))
    }

}