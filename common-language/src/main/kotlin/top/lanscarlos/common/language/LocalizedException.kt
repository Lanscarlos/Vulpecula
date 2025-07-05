package top.lanscarlos.common.language

import taboolib.common.platform.ProxyCommandSender
import java.lang.RuntimeException

/**
 * Vulpecula
 * top.lanscarlos.common.language
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
abstract class LocalizedException : RuntimeException() {

    /**
     * 语言节点
     * */
    abstract val node: String

    /**
     * 参数
     * */
    abstract val args: Array<Any>

    /**
     * 默认为空
     * */
    override val cause: Throwable? = null

    open fun getLocalizedMessage(sender: ProxyCommandSender): Message {
        val message = (cause as? LocalizedException)?.getLocalizedMessage(sender)
        return Message(node, args, message)
    }

}