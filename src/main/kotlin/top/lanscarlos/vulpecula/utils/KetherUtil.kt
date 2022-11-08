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
        val block = mutableListOf<ParsedAction<*>>()
        while (!this.hasNextToken("}")) {
            block += this.nextParsedAction()
        }
        ParsedAction(ActionBlock(block))
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
fun ScriptFrame.setVariable(key: String, value: Any?) {
    return variables().set(key, value)
}

/**
 * 设置变量
 * */
fun ScriptContext.setVariable(vararg keys: String, value: Any?) {
    keys.forEach { key ->
        set(key, value)
    }
}

fun QuestReader.readInt(demarcation: String): Any {
    return if (this.hasNextToken(demarcation)) {
        this.nextBlock()
    } else {
        this.nextInt()
    }
}

fun QuestReader.readDouble(demarcation: String): Any {
    return if (this.hasNextToken(demarcation)) {
        this.nextBlock()
    } else {
        this.nextDouble()
    }
}

fun QuestReader.readString(demarcation: String): Any {
    return if (this.hasNextToken(demarcation)) {
        this.nextBlock()
    } else {
        this.nextToken()
    }
}

fun QuestReader.readVector(): Any {
    this.mark()
    return when (this.nextToken()) {
        "to" -> this.nextBlock()
        "from" -> {
            Triple(
                this.nextBlock(),
                this.nextBlock(),
                this.nextBlock()
            )
        }
        else -> {
            this.reset()
            Vector(
                this.nextDouble(),
                this.nextDouble(),
                this.nextDouble()
            )
        }
    }
}

fun QuestReader.readColor(): Any {
    this.mark()
    return when (this.nextToken()) {
        "rgb" -> {
            if (this.hasNextToken("using")) {
                Triple(
                    this.nextBlock(),
                    this.nextBlock(),
                    this.nextBlock()
                )
            } else {
                Color(
                    this.nextInt(),
                    this.nextInt(),
                    this.nextInt()
                )
            }
        }
        "hex" -> {
            if (this.hasNextToken("using")) {
                this.nextBlock()
            } else {
                val hex = this.nextToken()
                Color.decode(if (hex.startsWith('#')) hex.substring(1) else hex)
            }
        }
        else -> {
            this.reset()
            Color.WHITE
        }
    }
}

fun ScriptFrame.coerceParticle(input: Any?, def: ProxyParticle): ProxyParticle {
    return when (input) {
        is ProxyParticle -> input
        is ParsedAction<*> -> {
            this.run(input).join()?.toString()?.let { p ->
                ProxyParticle.valueOf(p.uppercase())
            } ?: def
        }
        is String -> ProxyParticle.valueOf(input)
        else -> def
    }
}

fun ScriptFrame.coerceInt(input: Any?, def: Int): Int {
    return when (input) {
        is Int -> input
        is ParsedAction<*> -> this.run(input).join().toInt(def)
        else -> input.toInt(def)
    }
}

fun ScriptFrame.coerceDouble(input: Any?, def: Double): Double {
    return when (input) {
        is Int -> input.toDouble()
        is Double -> input
        is ParsedAction<*> -> this.run(input).join().toDouble(def)
        else -> input.toDouble(def)
    }
}

fun ScriptFrame.coerceString(input: Any?, def: String): String {
    return when (input) {
        is String -> input
        is Number -> input.toString()
        is ParsedAction<*> -> this.run(input).join()?.toString() ?: def
        else -> input?.toString() ?: def
    }
}

fun ScriptFrame.coerceVector(input: Any?, def: Vector): Vector {
    return when (input) {
        is Vector -> input
        is org.bukkit.util.Vector -> Vector(input.x, input.y, input.z)
        is ParsedAction<*> -> {
            when (val result = this.run(input).join()) {
                is Vector -> result
                is org.bukkit.util.Vector -> Vector(result.x, result.y, result.z)
                is Location -> result.toVectorFix()
                is org.bukkit.Location -> result.toProxyLocation().toVectorFix()
                else -> def
            }
        }
        is Triple<*, *, *> -> {
            Vector(
                (input.first as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.x) } ?: def.x,
                (input.second as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.y) } ?: def.y,
                (input.third as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.z) } ?: def.z
            )
        }
        else -> def
    }
}

fun ScriptFrame.coerceLocation(input: Any?, def: Location): Location {
    return when (input) {
        is Location -> input
        is org.bukkit.Location -> input.toProxyLocation()
        is Entity -> input.location.toProxyLocation()
        is ParsedAction<*> -> {
            when (val result = this.run(input).join()) {
                is Location -> result
                is org.bukkit.Location -> result.toProxyLocation()
                is Entity -> result.location.toProxyLocation()
                else -> def
            }
        }
        is Triple<*, *, *> -> {
            when (input.first) {
                is Double -> Location(def.world, input.first.toDouble(0.0), input.second.toDouble(0.0), input.third.toDouble(0.0))
                is ParsedAction<*> -> {
                    Location(
                        def.world,
                        (input.first as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
                        (input.second as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
                        (input.third as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
                    )
                }
                else -> def
            }
        }
        else -> def
    }
}

fun ScriptFrame.coerceColor(input: Any?, def: Color): Color {
    return when (input) {
        is Color -> input
        is ParsedAction<*> -> {
            this.run(input).join()?.toString()?.let { hex ->
                Color.decode(if (hex.startsWith('#')) hex.substring(1) else hex)
            } ?: def
        }
        else -> def
    }
}