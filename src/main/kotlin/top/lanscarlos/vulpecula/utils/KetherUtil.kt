package top.lanscarlos.vulpecula.utils

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-02-27 10:48
 */

/**
 * 尝试通过前缀解析 Action
 * */
fun QuestReader.tryNextAction(prefix: String): ParsedAction<*>? {
    return try {
        this.mark()
        this.expect(prefix)
        this.next(ArgTypes.ACTION)
    } catch (e: Exception) {
        this.reset()
        null
    }
}

/**
 * 尝试通过前缀解析 Action List
 * */
fun QuestReader.tryNextActionList(prefix: String): List<ParsedAction<*>> {
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
@Deprecated("This method is not recommended")
fun List<ParsedAction<*>>.run(frame: ScriptFrame): Map<String, String> {
    return this.associate {
        val arg = it.run(frame).toString()
        val array = arg.split("=")
        if (array.size != 2) error("Illegal Arg: \"$arg\" in action \"${this::class.simpleName}\"")
        array[0] to array[1]
    }
}

/**
 * 获取变量
 * */
fun <T> QuestContext.Frame.getVariable(key: String): T? {
    val result = variables().get<T>(key)
    return if (result.isPresent) result.get() else null
}

/**
 * 获取变量
 * */
fun <T> QuestContext.Frame.getVariable(vararg keys: String): T? {
    keys.forEach { key ->
        val result = variables().get<T>(key)
        if (result.isPresent) {
            return result.get()
        }
    }
    return null
}

/**
 * 设置变量
 * */
fun QuestContext.Frame.setVariable(key: String, value: Any?) {
    return variables().set(key, value)
}