package top.lanscarlos.vulpecula.bacikal.action.event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.event
 *
 * @author Lanscarlos
 * @since 2023-03-23 16:13
 */
object ActionEventName : ActionEvent.Resolver {

    override val name: Array<String> = arrayOf("name")

    /**
     * event name
     * */
    override fun resolve(reader: ActionEvent.Reader): ActionEvent.Handler<out Any?> {
        return reader.handle {
            combine(
                source()
            ) { event ->
                event.eventName
            }
        }
    }
}