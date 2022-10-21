package top.lanscarlos.vulpecula.kether.action

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import taboolib.module.kether.switch
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.getVariable
import top.lanscarlos.vulpecula.utils.run
import top.lanscarlos.vulpecula.utils.toBoolean
import top.lanscarlos.vulpecula.utils.tryNextAction

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-10-21 10:13
 */
object ActionEvent {

    /**
     * event cancel
     * event cancel to {cancelled}
     *
     * event cancelled
     *
     * event name
     * */
    @VulKetherParser(
        id = "event",
        name = ["event"],
    )
    fun parser() = scriptParser { reader ->
        reader.mark()
        val source = if (reader.nextToken() !in arrayOf("cancel", "isCancelled", "cancelled", "eventName", "name")) {
            reader.reset()
            reader.next(ArgTypes.ACTION)
        } else {
            reader.reset()
            null
        }
        reader.switch {
            case("cancel") {
                val next = reader.tryNextAction("to")
                actionNow {
                    val cancelled = next?.run(this).toBoolean(true)
                    val event = source?.run(this) as? Event ?: this.getVariable<Event>("event", "@Event") ?: error("No event selected!")
                    (event as? Cancellable)?.isCancelled = cancelled
                    return@actionNow (event as? Cancellable)?.isCancelled
                }
            }
            case("cancelled") {
                actionNow {
                    val event = source?.run(this) as? Event ?: this.getVariable<Event>("event", "@Event") ?: error("No event selected!")
                    return@actionNow (event as? Cancellable)?.isCancelled
                }
            }
            case("name") {
                actionNow {
                    val event = source?.run(this) as? Event ?: this.getVariable<Event>("event", "@Event") ?: error("No event selected!")
                    return@actionNow event.eventName
                }
            }
        }
    }

}