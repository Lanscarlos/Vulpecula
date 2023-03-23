package top.lanscarlos.vulpecula.bacikal.action.event

import org.bukkit.event.Cancellable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.event
 *
 * @author Lanscarlos
 * @since 2023-03-23 16:13
 */
object ActionEventCancel : ActionEvent.Resolver {

    override val name: Array<String> = arrayOf("cancel")

    /**
     * event cancel
     * */
    override fun resolve(reader: ActionEvent.Reader): ActionEvent.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                optional("to", then = bool(), def = true)
            ) { event, cancelled ->
                (event as? Cancellable)?.isCancelled = cancelled
                event
            }
        }
    }
}