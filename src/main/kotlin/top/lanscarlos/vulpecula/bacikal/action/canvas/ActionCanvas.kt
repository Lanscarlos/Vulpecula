package top.lanscarlos.vulpecula.bacikal.action.canvas

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.bacikal
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas
 *
 * @author Lanscarlos
 * @since 2022-11-08 10:05
 */
object ActionCanvas {

    private const val NAMESPACE_EXTEND = "vulpecula-canvas"

    const val VARIABLE_TARGET = "@CanvasTarget"
    const val VARIABLE_DURATION_START = "@CanvasStartTime"
    const val VARIABLE_DURATION_END = "@CanvasEndTime"
    const val VARIABLE_BRUSH = "@CanvasBrush"
    const val VARIABLE_VIEWERS = "@CanvasViewers"
    const val VARIABLE_PATTERNS = "@CanvasPatterns"

    @BacikalParser(
        id = "canvas",
        name = ["canvas"],
    )
    fun parser() = bacikal {
        // 添加内部命名空间
        addNamespace(NAMESPACE_EXTEND)

        combineOf(
            argument("unique", "uuid", then = text(display = "unique id")),
            argument("force", then = bool(false), def = false),
            argument("bind", then = any()),
            argument("period", then = int(0), def = 0),
            argument("duration", then = int(200), def = 200),
            argument("viewers", "viewer", then = ActionViewers.viewers()),
            argument("pre-handle", "on-start", "pre", then = action()),
            argument("post-handle", "on-end", "post", then = action()),
            trim("then", then = action())
        ) { uuid, force, bind, period, duration, viewers, preHandle, postHandle, body ->

            // 设置绑定目标
            if (bind != null) {
                this.setVariable(VARIABLE_TARGET, viewers, deep = false)
                this.setVariable("target", viewers, deep = false)
            }

            // 设置观察者
            if (viewers != null) {
                this.setVariable(VARIABLE_VIEWERS, viewers, deep = false)
            }

            if (period <= 0) {
                // 执行周期小于零，直接执行
                return@combineOf when {
                    preHandle != null && postHandle != null -> {
                        this@combineOf.run(preHandle).thenCompose {
                            this@combineOf.run(body).thenCompose {
                                this@combineOf.run(postHandle)
                            }
                        }
                    }
                    preHandle != null -> {
                        this@combineOf.run(preHandle).thenCompose {
                            this@combineOf.run(body)
                        }
                    }
                    postHandle != null -> {
                        this@combineOf.run(body).thenCompose {
                            this@combineOf.run(postHandle)
                        }
                    }
                    else -> {
                        this@combineOf.run(body)
                    }
                }
            } else {

                // 组装 unique id
                val mainId = uuid ?: UUID.randomUUID().toString()
                val extendId = when (bind) {
                    is Block -> bind.location.toString()
                    is Entity -> bind.uniqueId.toString()
                    is BukkitPlayer -> bind.player.uniqueId.toString()
                    else -> bind?.toString()
                }
                val uniqueId =  if (extendId != null) mainId + '_' + extendId else mainId

                val quest = CanvasQuest(
                    uniqueId, period, duration, body,
                    preHandle ?: ParsedAction.noop<Any?>(),
                    postHandle ?: ParsedAction.noop<Any?>()
                )

                // 提交绘画任务
                CanvasScriptContext.submit(quest, this.deepVars(), force)
            }

            return@combineOf CompletableFuture.completedFuture(null)
        }
    }
}