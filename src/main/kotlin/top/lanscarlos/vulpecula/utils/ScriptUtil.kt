package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-08-19 16:38
 */


/**
 * 运行脚本
 * */
fun Script.runActions(): CompletableFuture<Any?> {
    return try {
        ScriptContext.create(this).runActions()
    } catch (e: Exception) {
        e.printKetherErrorMessage()
        CompletableFuture.completedFuture(null)
    }
}

/**
 * 运行脚本
 * */
fun Script.runActions(func: ScriptContext.() -> Unit): CompletableFuture<Any?> {
    return try {
        ScriptContext.create(this).apply(func).runActions()
    } catch (e: Exception) {
        e.printKetherErrorMessage()
        CompletableFuture.completedFuture(null)
    }
}

/**
 * 运行脚本
 * */
fun eval(
    script: String,
    sender: Any? = null,
    namespace: List<String> = listOf("vulpecula"),
    args: Map<String, Any?>? = null,
    throws: Boolean = false
): CompletableFuture<Any?> {
    val func = {
        KetherShell.eval(script, sender = sender?.let { adaptCommandSender(it) }, namespace = namespace, context= {
            args?.forEach { (k, v) -> set(k, v) }
        })
    }
    return if (throws) func()
    else try {
        func()
    } catch (e: Exception) {
        e.printKetherErrorMessage()
        CompletableFuture.completedFuture(false)
    }
}