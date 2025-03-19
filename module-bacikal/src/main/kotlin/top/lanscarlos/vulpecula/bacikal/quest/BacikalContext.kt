package top.lanscarlos.vulpecula.bacikal.quest

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-03-18 10:26
 */
interface BacikalContext {

    var sender: ProxyCommandSender?

    var senderAsPlayer: Player?

    val breakFlag: Boolean

    val exitFlag: Boolean

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
     * 获取变量集合
     * */
    fun variables(): MutableMap<String, Any>

    /**
     * 运行脚本
     * */
    fun runActions(): CompletableFuture<*>

    /**
     * 终止运行
     * */
    fun terminate()

}