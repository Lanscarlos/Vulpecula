package top.lanscarlos.vulpecula.utils

import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.script
import top.lanscarlos.vulpecula.kether.action.ActionBlock

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-02-27 10:48
 */


/**
 * 查看下一个 Token 但不改变位置
 * */
fun QuestReader.nextPeek(): String {
    this.mark()
    val token = this.nextToken()
    this.reset()
    return token
}

/**
 * 判断是否有指定 token
 * */
fun QuestReader.hasNextToken(vararg expected: String): Boolean {
    if (expected.isEmpty()) return false
    this.mark()
    return if (this.nextToken() in expected) {
        true
    } else {
        this.reset()
        false
    }
}

/**
 * 尝试通过前缀解析 Action
 * */
fun QuestReader.tryNextAction(vararg expected: String): ParsedAction<*>? {
    if (expected.isEmpty()) return null
    this.mark()
    return if (this.nextToken() in expected) {
        this.next(ArgTypes.ACTION)
    } else {
        this.reset()
        null
    }
}

/**
 * 尝试通过前缀解析 Action List
 * */
fun QuestReader.tryNextActionList(expected: String): List<ParsedAction<*>>? {
    this.mark()
    return if (this.nextToken() in expected) {
        this.next(ArgTypes.listOf(ArgTypes.ACTION))
    } else {
        this.reset()
        null
    }
}

/**
 * 通过兼容模式解析语句块
 * */
fun QuestReader.nextBlock(): ParsedAction<*> {
    return if (this.hasNextToken("{")) {
        ParsedAction(ActionBlock(ActionBlock.readBlock(reader = this)))
    } else {
        this.nextParsedAction()
    }
}

/**
 * 尝试通过前缀解析语句块
 * */
fun QuestReader.tryNextBlock(prefix: String): ParsedAction<*>? {
    this.mark()
    return if (this.nextToken() in prefix) {
        this.nextBlock()
    } else {
        this.reset()
        null
    }
}

fun ParsedAction<*>.run(frame: ScriptFrame): Any? {
    return frame.newFrame(this).run<Any?>().get()
}

/**
 * 获取变量
 * */
fun <T> ScriptFrame.getVariable(key: String): T? {
    val result = variables().get<T>(key)
    return if (result.isPresent) result.get() else null
}

/**
 * 获取变量
 * */
fun <T> ScriptFrame.getVariable(vararg keys: String): T? {
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
fun ScriptFrame.setVariable(key: String, value: Any?, deep: Boolean = true) {
    var root = this
    while (root.parent().isPresent) {
        root = root.parent().get()
    }
    return root.variables().set(key, value)
}

/**
 * 设置变量
 * */
fun ScriptContext.setVariable(vararg keys: String, value: Any?) {
    keys.forEach { key ->
        set(key, value)
    }
}

fun QuestContext.Frame.unsafePlayer(): ProxyPlayer? {
    return script().sender as? ProxyPlayer
}