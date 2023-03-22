package top.lanscarlos.vulpecula.bacikal.action.location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-21 15:06
 */
object ActionLocationDistance : ActionLocation.Resolver {

    override val name: Array<String> = arrayOf("distance", "dist")

    /**
     * loc distance &loc with/to &target
     * loc distance &loc with/to x,y,z
     * */
    override fun resolve(reader: ActionLocation.Reader): ActionLocation.Handler<out Any?> {
        return reader.handle {
            combine(
                reader.source(),
                trim("with", "to", then = location(display = "location target"))
            ) { location, target ->
                location.distance(target)
            }
        }
    }
}