package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.info
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.module.script.exception.ScriptExecuteException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/5/20 13:55
 */
abstract class AbstractScript : Script {

    abstract val quest: Quest

    abstract fun execute(sender: ProxyCommandSender?, args: List<Any?>, variables: Map<String, Any>): ScriptTask

    override fun run(sender: ProxyCommandSender?, args: List<Any?>, variables: Map<String, Any>): ScriptTask {
        return try {
            execute(sender, args, variables)
        } catch (e: ScriptExecuteException) {
            // 消除重复嵌套
            throw e
        } catch (e: Exception) {
            throw ScriptExecuteException(this.id, e)
        }
    }

}