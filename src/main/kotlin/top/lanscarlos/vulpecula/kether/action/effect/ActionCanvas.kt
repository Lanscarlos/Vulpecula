package top.lanscarlos.vulpecula.kether.action.effect

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.kether.ParsedAction
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionBlock
import top.lanscarlos.vulpecula.utils.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.effect
 *
 * @author Lanscarlos
 * @since 2022-11-08 10:05
 */
class ActionCanvas : ScriptAction<Any?>() {

    var unique: Any? = null
    var force: Boolean = false
    var period: Int = 20
    var condition: ParsedAction<*>? = null
    val actions = mutableListOf<ParsedAction<*>>()
    var preHandle = mutableListOf<ParsedAction<*>>()
    var postHandle = mutableListOf<ParsedAction<*>>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {

        /*
        * 获取本次任务的 id
        * */
        val uniqueId = when (val it = unique) {
            "false" -> "temp_" + UUID.randomUUID().toString()
            is String -> it + '_' + frame.player().uniqueId.toString()
            is Pair<*, *> -> {
                val id = it.first?.toString() ?: "temp"
                val extend = (it.second as? ParsedAction<*>)?.let { action ->
                    when (val result = frame.run(action).join()) {
                        is Block -> result.location.toString()
                        is Entity -> result.uniqueId.toString()
                        else -> result?.toString()
                    }
                }
                id + '_' + extend
            }
            else -> null
        } ?: frame.player().uniqueId.toString()

        frame.setVariable("@Brush", CanvasBrush())

        condition?.let {
            val body = ParsedAction(ActionBlock(actions))
            val preHandle = ParsedAction(ActionBlock(this.preHandle))
            val postHandle = ParsedAction(ActionBlock(this.postHandle))
            val quest = CanvasQuest(uniqueId, period, it, body, preHandle, postHandle)
            // 提交绘画任务
            CanvasScriptContext.submit(quest, frame.deepVars(), force)
        } ?: warning("Canvas Condition not defined.")

        return CompletableFuture.completedFuture(null)
    }

    companion object {

        private const val extendNamespace = "vulpecula-canvas"

        @VulKetherParser(
            id = "canvas",
            name = ["canvas"]
        )
        fun parser() = scriptParser { reader ->

            // 添加内部命名空间
            val namespace = reader.getProperty<MutableList<String>>("namespace")!!
            namespace += extendNamespace

            val canvas = ActionCanvas()

            if (!reader.hasNextToken("{")) {
                return@scriptParser canvas
            }

            while (!reader.hasNextToken("}")) {
                reader.mark()
                when (reader.nextToken().lowercase()) {
                    "unique" -> {
                        val id = reader.nextToken()
                        canvas.unique = if (reader.hasNextToken("with")) {
                            Pair(id, reader.nextBlock())
                        } else {
                            id
                        }
                    }
                    "period" -> {
                        canvas.period = reader.nextInt()
                    }
                    "force" -> {
                        canvas.force = reader.nextToken().toBoolean(false)
                    }
                    "condition" -> {
                        canvas.condition = reader.nextBlock()
                    }
                    "pre-handle", "on-start" -> {
                        canvas.preHandle += reader.nextBlock()
                    }
                    "post-handle", "on-end" -> {
                        canvas.postHandle += reader.nextBlock()
                    }
                    else -> {
                        reader.reset()
                        canvas.actions += reader.nextBlock()
                    }
                }
            }

            // 移除内部命名空间
            namespace -= extendNamespace

            canvas
        }
    }
}