package top.lanscarlos.vulpecula.module.script

import taboolib.module.kether.ScriptContext
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * 代表一个正在运行中的脚本任务
 *
 * @author Lanscarlos
 * @since 2025/4/27 14:02
 */
interface ScriptTask {

    val pid: Long

    val script: Script

    val context: ScriptContext

    val future: CompletableFuture<Any?>

    val startTime: Long

    val isDone: Boolean

    fun terminate()

}