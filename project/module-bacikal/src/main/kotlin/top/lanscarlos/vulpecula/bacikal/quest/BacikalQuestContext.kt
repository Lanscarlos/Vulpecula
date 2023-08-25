package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:42
 */
interface BacikalQuestContext {

    /**
     * 任务
     * */
    val quest: BacikalQuest

    /**
     * 脚本执行者
     * */
    var sender: ProxyCommandSender?

    /**
     * 初始化变量
     * */
    fun initVariables(variables: Map<String, Any>)

    /**
     * 获取变量
     * */
    fun <T> getVariable(key: String): T?

    /**
     * 设置变量
     * */
    fun setVariable(key: String, value: Any)

    /**
     * 设置变量
     * */
    fun setVariables(vararg key: String, value: Any)

    /**
     * 执行脚本
     * */
    fun runActions(): CompletableFuture<Any?>

    /**
     * 终止脚本
     * */
    fun terminate()

}