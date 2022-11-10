package top.lanscarlos.vulpecula.utils

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.function.info
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.kether.action.ActionBlock
import java.awt.Color

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
fun QuestReader.tryNextAction(vararg prefix: String): ParsedAction<*>? {
    this.mark()
    return if (this.nextToken() in prefix) {
        this.next(ArgTypes.ACTION)
    } else {
        this.reset()
        null
    }
}

/**
 * 尝试通过前缀解析 Action List
 * */
fun QuestReader.tryNextActionList(prefix: String): List<ParsedAction<*>>? {
    this.mark()
    return if (this.nextToken() in prefix) {
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
 * 运行一组动作
 * @return 返回最后一个动作的结果
 * */
@Deprecated("Deprecated")
fun List<ParsedAction<*>>.run(frame: ScriptFrame): Any? {
    return if (this.isEmpty()) {
        null
    } else if (this.size == 1) {
        this[0].run(frame)
    } else {
        for (i in 0 until lastIndex) {
            this[i].run(frame)
        }
        this[lastIndex].run(frame)
    }
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