package top.lanscarlos.vulpecula.module.action.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.event
 *
 * @author Lanscarlos
 * @since 2025/6/15
 */
@BacikalParser("event.cancel")
object EventCancelAction : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        frame.setVariable("@EVENT_STATUS", "CANCELED")
        val event = frame.getVariable<Event>("@EVENT")
        if (event is Cancellable) {
            event.isCancelled = true
        }
    }

}