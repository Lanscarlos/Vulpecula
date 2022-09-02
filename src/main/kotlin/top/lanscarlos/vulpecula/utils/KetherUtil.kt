package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.function.adaptCommandSender
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-02-27 10:48
 */

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

fun QuestReader.tryNextAction(prefix: String = "by"): ParsedAction<*>? {
    return try {
        this.mark()
        this.expect(prefix)
        this.next(ArgTypes.ACTION)
    } catch (e: Exception) {
        this.reset()
        null
    }
}

fun QuestReader.tryNextArgs(prefix: String = "with"): List<ParsedAction<*>> {
    return try {
        this.mark()
        this.expect(prefix)
        this.next(ArgTypes.listOf(ArgTypes.ACTION))
    } catch (e: Exception) {
        this.reset()
        listOf()
    }
}

fun ParsedAction<*>.run(frame: ScriptFrame): Any? {
    return frame.newFrame(this).run<Any?>().get()
}

/**
 * 将 [ arg1=value1 arg2=value2 ... ] 转化为 Map<String, String> 集合
 * */
fun List<ParsedAction<*>>.run(frame: ScriptFrame): Map<String, String> {
    return this.associate {
        val arg = it.run(frame).toString()
        val array = arg.split("=")
        if (array.size != 2) error("Illegal Arg: \"$arg\" in action \"${this::class.simpleName}\"")
        array[0] to array[1]
    }
}

fun Map<String, String>.variable(vararg keys: String): String? {
    keys.forEach { key ->
        this[key]?.let { return it }
    }
    return null
}

fun QuestContext.Frame.variable(key: String): Any? {
    return variables().get<Any?>(key).let { if (it.isPresent) it.get() else null }
}

fun QuestContext.Frame.variable(key: String, value: Any?) {
    return variables().set(key, value)
}