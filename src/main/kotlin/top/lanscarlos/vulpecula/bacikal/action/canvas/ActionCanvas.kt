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
            argument("preprocessing", "pre", then = action(), def = ParsedAction.noop<Any?>()),
            argument("postprocessing", "post", then = action(), def = ParsedAction.noop<Any?>()),
            trim("then", then = action())
        ) { uuid, force, bind, period, duration, viewers, preprocessing, postprocessing, body ->

            // 设置绑定目标
            if (bind != null) {
                this.variables().set(VARIABLE_TARGET, bind)
                this.variables().set("target", bind)
            }

            // 设置观察者
            if (viewers != null) {
                this.variables().set(VARIABLE_VIEWERS, viewers)
            }

            if (period <= 0) {
                // 执行周期小于零，直接执行
                return@combineOf this.newFrame(preprocessing).run<Any?>().thenCompose {
                    this.newFrame(body).run<Any?>().thenCompose {
                        this.newFrame(postprocessing).run<Any?>().thenRun {
                            // 删除变量
                            this.variables().remove(VARIABLE_TARGET)
                            this.variables().remove("target")
                            this.variables().remove(VARIABLE_DURATION_START)
                            this.variables().remove(VARIABLE_DURATION_END)
                            this.variables().remove(VARIABLE_BRUSH)
                            this.variables().remove(VARIABLE_VIEWERS)
                            this.variables().remove(VARIABLE_PATTERNS)
                        }
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
                val quest = CanvasQuest(uniqueId, period, duration, body, preprocessing, postprocessing)

                // 提交绘画任务
                CanvasScriptContext.submit(quest, this.deepVars(), force)

                // 删除变量
                this.variables().remove(VARIABLE_TARGET)
                this.variables().remove("target")
                this.variables().remove(VARIABLE_DURATION_START)
                this.variables().remove(VARIABLE_DURATION_END)
                this.variables().remove(VARIABLE_BRUSH)
                this.variables().remove(VARIABLE_VIEWERS)
                this.variables().remove(VARIABLE_PATTERNS)
            }

            return@combineOf CompletableFuture.completedFuture(null)
        }
    }
}