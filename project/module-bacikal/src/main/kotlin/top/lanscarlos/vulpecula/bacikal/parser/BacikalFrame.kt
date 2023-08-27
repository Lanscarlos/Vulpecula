package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ParsedAction
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2023-08-27 22:06
 */
interface BacikalFrame {

    var sender: ProxyCommandSender?

    var senderAsPlayer: ProxyPlayer?

    operator fun get(key: String): Any?

    operator fun set(key: String, value: Any?): Any?

    /**
     * 获取变量
     * */
    fun <T> getVariable(key: String): T?

    /**
     * 获取变量，如果不存在则返回默认值
     * */
    fun <T> getVariable(key: String, default: T): T

    /**
     * 获取多个变量
     * @return 第一个不为空的变量
     * */
    fun <T> getVariables(vararg key: String): T?

    /**
     * 设置变量
     * @return 旧值
     * */
    fun setVariable(key: String, value: Any?): Any?

    /**
     * 设置多个变量
     * */
    fun setVariables(vararg key: String, value: Any?)

    /**
     * 创建新的 Frame
     * */
    fun newFrame(name: String): BacikalFrame

    /**
     * 创建新的 Frame
     * */
    fun newFrame(action: ParsedAction<*>): BacikalFrame

    /**
     * 执行动作
     * @param newFrame 是否创建新的 Frame
     * */
    fun <T> runAction(action: ParsedAction<T>, newFrame: Boolean = false): CompletableFuture<T>

}