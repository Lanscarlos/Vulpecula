package top.lanscarlos.vulpecula.common.exception

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.module.lang.sendErrorMessage
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/11/22
 */
abstract class AbstractLocalizedException : RuntimeException() {

    /**
     * 语言节点
     */
    abstract val lang: Lang

    /**
     * 参数
     */
    open val arguments: Array<Any> = emptyArray()

    /**
     * 默认以控制台视角获取信息
     */
    override val message: String?
        get() = getLocalizedMessage(console())

    /**
     * 获取本地化语言信息
     */
    open fun getLocalizedMessage(receiver: ProxyCommandSender): String {
        return lang.asText(receiver, *arguments)
    }

    /**
     * 将异常信息通知指定接收者
     */
    open fun notice(receiver: ProxyCommandSender) {
        receiver.sendErrorMessage(getLocalizedMessage(receiver))
    }

}