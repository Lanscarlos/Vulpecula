package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/15
 */
//@BacikalParser
object EventIgnoreAction : BacikalActionResolver {

    override val bind: String? = null

    override val id: String = "ignore"

    fun resolve(frame: BacikalFrame) {
        frame.setVariable("@EVENT_STATUS", "CANCELED")
        val event = frame.getVariable<Event>("@EVENT")
        if (event is Cancellable) {
            event.isCancelled = true
        }
    }

}