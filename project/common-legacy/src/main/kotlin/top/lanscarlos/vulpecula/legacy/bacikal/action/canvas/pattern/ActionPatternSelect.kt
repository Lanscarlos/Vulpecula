package top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.pattern

import top.lanscarlos.vulpecula.legacy.bacikal.action.canvas.ActionCanvas
import top.lanscarlos.vulpecula.legacy.utils.getVariable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2023-06-29 14:19
 */
object ActionPatternSelect : ActionPattern.PatternResolver {

    override val name: Array<String> = arrayOf("select", "sel")

    override fun resolve(reader: ActionPattern.Reader): ActionPattern.Handler<CanvasPattern> {
        return reader.handle {
            combine(
                int(display = "pattern index")
            ) { index ->
                val patterns = this.getVariable<MutableList<CanvasPattern>>(ActionCanvas.VARIABLE_PATTERNS) ?: error("No patterns selected.")
                patterns.getOrNull(index - 1) ?: error("Illegal pattern index \"$index\" at pattern action.")
            }
        }
    }

}