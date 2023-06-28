package top.lanscarlos.vulpecula.bacikal.action.canvas

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.common.platform.function.submit
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
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
    const val VARIABLE_DURATION_START = "@CanvasStartTime"
    const val VARIABLE_DURATION_END = "@CanvasEndTime"
    const val VARIABLE_BRUSH = "@CanvasBrush"
    const val VARIABLE_ORIGIN = "@CanvasOrigin"
    const val VARIABLE_VIEWERS = "@CanvasViewers"
    const val VARIABLE_PATTERN = "@CanvasPattern"

    @BacikalParser(
        id = "canvas",
        name = ["canvas"],
    )
    fun parser() = bacikal {
        // 添加内部命名空间
        addNamespace(NAMESPACE_EXTEND)

        combineOf(
            argument("unique", "uuid", then = text(display = "unique id").union(optional("with", then = any()))),
            argument("force", then = bool(false), def = false),
            argument("period", then = int(0), def = 0),
            argument("viewers", "viewer", then = ActionViewers.viewers()),
            argument("pre-handle", "on-start", "pre", then = action()),
            argument("post-handle", "on-end", "post", then = action()),
            trim("then", then = action())
        ) { uuid, force, period, viewers, preHandle, postHandle, body ->

            // 设置观察者
            if (viewers != null) {
                this.setVariable(VARIABLE_VIEWERS, viewers)
            }

            if (period <= 0) {
                // 执行周期小于零，直接异步执行
                submit(async = true) {
                    when {
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
                }
//            warning("Canvas Condition not defined.")
            } else {

                // 组装 unique id
                val mainId = uuid?.first ?: UUID.randomUUID().toString()
                val extendId = when (val it = uuid?.second) {
                    is Block -> it.location.toString()
                    is Entity -> it.uniqueId.toString()
                    else -> it?.toString()
                }
                val uniqueId =  if (extendId != null) mainId + '_' + extendId else mainId

                val quest = CanvasQuest(
                    uniqueId,
                    period,
                    body,
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