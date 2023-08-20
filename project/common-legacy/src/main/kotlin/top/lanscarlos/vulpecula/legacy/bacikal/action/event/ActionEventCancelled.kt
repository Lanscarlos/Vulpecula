package top.lanscarlos.vulpecula.legacy.bacikal.action.event

import org.bukkit.event.Cancellable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.event
 *
 * @author Lanscarlos
 * @since 2023-03-23 16:13
 */
object ActionEventCancelled : ActionEvent.Resolver {

    override val name: Array<String> = arrayOf("cancelled")

    /**
     * event cancelled
     * */
    override fun resolve(reader: ActionEvent.Reader): ActionEvent.Handler<out Any?> {
        return reader.handle {
            combine(
                source()
            ) { event ->
                (event as? Cancellable)?.isCancelled ?: false
            }
        }
    }
}