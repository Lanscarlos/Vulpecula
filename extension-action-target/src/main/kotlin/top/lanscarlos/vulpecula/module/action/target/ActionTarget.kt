package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/3
 */
object ActionTarget {

    private const val CONTEXT = "@VULPECULA_CONTEXT_TARGET"

    fun getContext(frame: BacikalFrame): LinkedList<Any> {
        val target = frame.getVariable<LinkedList<Any>>(CONTEXT)
            ?: error(asLang("module-action-target-exception-context-not-found"))
        return target
    }

    fun setContext(frame: BacikalFrame, targets: Collection<Any>) {
        frame.setVariable(CONTEXT, LinkedList(targets))
    }

    fun setContext(frame: BacikalFrame, target: Player) {
        frame.setVariable(CONTEXT, LinkedList<Any>().also { it.add(target) })
    }

    fun setContext(frame: BacikalFrame, target: Entity) {
        frame.setVariable(CONTEXT, LinkedList<Any>().also { it.add(target) })
    }

    fun setContext(frame: BacikalFrame, target: Location) {
        frame.setVariable(CONTEXT, LinkedList<Any>().also { it.add(target) })
    }

    fun setContext(frame: BacikalFrame, target: taboolib.common.util.Location) {
        frame.setVariable(CONTEXT, LinkedList<Any>().also { it.add(target) })
    }

}