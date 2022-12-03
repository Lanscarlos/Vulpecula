package top.lanscarlos.vulpecula.kether.action.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

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
     *
     * event wait/require xxx {...} 等待某事件发生
     * */
    @VulKetherParser(
        id = "event",
        name = ["event"]
    )
    fun parser() = scriptParser { reader ->
        reader.mark()
        val source = if (reader.nextToken() !in arrayOf("cancel", "isCancelled", "cancelled", "eventName", "name", "wait", "require")) {
            reader.reset()
            reader.next(ArgTypes.ACTION)
        } else {
            reader.reset()
            null
        }
        reader.switch {
            case("cancel") {
                val next = reader.tryNextAction("to")
                actionTake {
                    listOf(
                        source?.let { this.run(it) },
                        next?.let { this.run(it) },
                    ).thenTake().thenApply {
                        val event = it[0] as? Event ?: this.getVariable<Event>("event", "@Event") ?: error("No event selected!")
                        val cancelled = it[1].coerceBoolean(true)

                        (event as? Cancellable)?.isCancelled = cancelled
                        return@thenApply (event as? Cancellable)?.isCancelled
                    }
                }
            }
            case("cancelled") {
                actionTake {
                    listOf(
                        source?.let { this.run(it) }
                    ).thenTake().thenApply {
                        val event = it[0] as? Event ?: this.getVariable<Event>("event", "@Event") ?: error("No event selected!")

                        return@thenApply (event as? Cancellable)?.isCancelled
                    }
                }
            }
            case("name") {
                actionTake {
                    if (source != null) {
                        this.run(source).thenApply {
                            (it as? Event)?.eventName ?: error("No event selected!")
                        }
                    } else {
                        CompletableFuture.completedFuture(
                            this.getVariable<Event>("event", "@Event")?.eventName ?: error("No event selected!")
                        )
                    }
                }
            }
            case("wait", "require") {
                ActionEventWait.read(this)
            }
        }
    }

}