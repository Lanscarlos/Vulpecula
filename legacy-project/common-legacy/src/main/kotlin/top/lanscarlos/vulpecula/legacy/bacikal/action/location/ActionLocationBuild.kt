package top.lanscarlos.vulpecula.legacy.bacikal.action.location

import taboolib.common.util.Location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-20 21:25
 */
object ActionLocationBuild : ActionLocation.Resolver {

    override val name: Array<String> = arrayOf("build", "create")

    /**
     * loc build &world &x &y &z
     * loc build &world &x &y &z and &yaw &pitch
     * */
    override fun resolve(reader: ActionLocation.Reader): ActionLocation.Handler<out Any?> {
        return reader.transfer {
            combine(
                text(),
                double(),
                double(),
                double(),
                optional("and", then = float().union(float()))
            ) { world, x, y, z, addition ->
                Location(world, x, y, z, addition?.first ?: 0f, addition?.second ?: 0f)
            }
        }
    }
}