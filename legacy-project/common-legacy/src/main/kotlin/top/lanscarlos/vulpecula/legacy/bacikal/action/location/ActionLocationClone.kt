package top.lanscarlos.vulpecula.legacy.bacikal.action.location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-20 21:22
 */
object ActionLocationClone : ActionLocation.Resolver {

    override val name: Array<String> = arrayOf("clone")

    override fun resolve(reader: ActionLocation.Reader): ActionLocation.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
            ) { location ->
                location.clone()
            }
        }
    }
}