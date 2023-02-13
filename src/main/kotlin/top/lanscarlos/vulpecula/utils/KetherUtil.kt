package top.lanscarlos.vulpecula.utils

import com.mojang.datafixers.kinds.App
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.*
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.kether.ParserBuilder
import top.lanscarlos.vulpecula.kether.action.ActionBlock
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-02-27 10:48
 */

fun String.toKetherScript(namespace: List<String> = emptyList()): Script {
    return this.parseKetherScript(namespace.plus("vulpecula"))
}

/**
 * @see taboolib.library.kether.Parser.build
 * */
@Suppress("UNCHECKED_CAST")
fun <A> buildParser(builder: ParserBuilder.(QuestReader) -> App<Parser.Mu, Parser.Action<A>>): ScriptActionParser<A> {
    val parser =  object : QuestActionParser {
        override fun <A> resolve(resolve: QuestReader): QuestAction<A> {
            val reader = (builder(object : ParserBuilder {}, resolve) as Parser<Parser.Action<A>>).reader
            val action = reader.apply(resolve) as Parser.Action<Parser.Action<A>>
            return object : QuestAction<A>() {
                override fun process(frame: QuestContext.Frame): CompletableFuture<A> {
                    return action.run(frame).thenCompose {
                        it.run(frame)
                    }
                }
            }
        }
    }
    return ScriptActionParser { parser.resolve<A>(this) }
}


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
 * 尝试通过前缀获取 Token
 * */
fun QuestReader.tryNextToken(vararg expected: String): String? {
    return if (this.hasNextToken(*expected)) this.nextToken() else null
}

/**
 * 尝试通过前缀解析 Action
 * */
fun QuestReader.tryNextAction(vararg expected: String): ParsedAction<*>? {
    return if (this.hasNextToken(*expected)) this.nextParsedAction() else null
}

/**
 * 尝试通过前缀解析 Action List
 * */
fun QuestReader.tryNextActionList(vararg expected: String): List<ParsedAction<*>>? {
    return if (this.hasNextToken(*expected)) this.next(ArgTypes.listOf(ArgTypes.ACTION)) else null
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
fun QuestReader.tryNextBlock(vararg expected: String): ParsedAction<*>? {
    return if (this.hasNextToken(*expected)) this.nextBlock() else null
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
    if (deep) {
        var root = this
        while (root.parent().isPresent) {
            root = root.parent().get()
        }
        root.variables().set(key, value)
    } else {
        this.variables().set(key, value)
    }
}

/**
 * 设置变量
 * */
fun ScriptContext.setVariable(vararg keys: String, value: Any?) {
    keys.forEach { key ->
        set(key, value)
    }
}

fun QuestContext.Frame.playerOrNull(): ProxyPlayer? {
    return script().sender as? ProxyPlayer
}

fun ProxyPlayer.toBukkit(): Player? {
    return (this as? BukkitPlayer)?.player
}