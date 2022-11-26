package top.lanscarlos.vulpecula.kether.action.canvas

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.common.platform.function.submit
import taboolib.library.kether.ParsedAction
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.action.ActionBlock
import top.lanscarlos.vulpecula.kether.live.BooleanLiveData
import top.lanscarlos.vulpecula.kether.live.IntLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-08 10:05
 */
class ActionCanvas : ScriptAction<Any?>() {

    var uniqueMain: LiveData<String>? = null
    var uniqueExtend: ParsedAction<*>? = null
    var force: LiveData<Boolean> = BooleanLiveData(false)
    var period: LiveData<Int> = IntLiveData(20)
    var condition: ParsedAction<*>? = null
    val actions = mutableListOf<ParsedAction<*>>()
    var preHandle = mutableListOf<ParsedAction<*>>()
    var postHandle = mutableListOf<ParsedAction<*>>()

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return listOf(
            uniqueMain?.getOrNull(frame),
            uniqueExtend?.let { frame.run(it) },
            force.getOrNull(frame),
            period.getOrNull(frame)
        ).thenTake().thenApply { args ->

            // 处理绘画任务 ID
            val mainId = args[0]?.toString() ?: frame.unsafePlayer()?.uniqueId?.toString() ?: error("No canvas unique id selected.")
            val extendId = when (val it = args[1]) {
                is Block -> it.location.toString()
                is Entity -> it.uniqueId.toString()
                else -> it?.toString()
            }

            val uniqueId =  if (extendId != null) mainId + '_' + extendId else mainId
            val force = args[2].toBoolean(false)
            val period = args[3].toInt(20)
            val condition = this.condition
            val body = ParsedAction(ActionBlock(actions))
            val preHandle = ParsedAction(ActionBlock(this.preHandle))
            val postHandle = ParsedAction(ActionBlock(this.postHandle))

            if (period <= 0 || condition == null) {
                // 循环条件未定义 任务只执行一次
                submit(async = true) {
                    frame.run(preHandle).thenRun {
                        frame.run(body).thenRun {
                            frame.run(postHandle)
                        }
                    }
                }
//            warning("Canvas Condition not defined.")
            } else {
                val quest = CanvasQuest(uniqueId, period, condition, body, preHandle, postHandle)
                // 提交绘画任务
                CanvasScriptContext.submit(quest, frame.deepVars(), force)
            }

            return@thenApply null
        }
    }

    companion object {

        private const val NAMESPACE_EXTEND = "vulpecula-canvas"
        const val VARIABLE_DURATION_START = "@CanvasStartTime"
        const val VARIABLE_DURATION_END = "@CanvasEndTime"
        const val VARIABLE_BRUSH = "@CanvasBrush"
        const val VARIABLE_ORIGIN = "@CanvasOrigin"
        const val VARIABLE_VIEWERS = "@CanvasViewers"
        const val VARIABLE_PATTERN = "@CanvasPattern"

        @VulKetherParser(
            id = "canvas",
            name = ["canvas"]
        )
        fun parser() = scriptParser { reader ->

            // 添加内部命名空间
            val namespace = reader.getProperty<MutableList<String>>("namespace")!!
            namespace += NAMESPACE_EXTEND

            val canvas = ActionCanvas()

            if (!reader.hasNextToken("{")) {
                return@scriptParser canvas
            }

            while (!reader.hasNextToken("}")) {
                reader.mark()
                when (reader.nextToken().lowercase()) {
                    "unique" -> {
                        canvas.uniqueMain = reader.readString()
                        if (reader.hasNextToken("with")) {
                            canvas.uniqueExtend = reader.nextBlock()
                        }
                    }
                    "period" -> {
                        canvas.period = reader.readInt()
                    }
                    "force" -> {
                        canvas.force = reader.readBoolean()
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
            namespace -= NAMESPACE_EXTEND

            return@scriptParser canvas
        }
    }
}