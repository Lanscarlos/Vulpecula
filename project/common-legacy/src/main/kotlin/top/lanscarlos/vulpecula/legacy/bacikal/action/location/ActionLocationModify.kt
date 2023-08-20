package top.lanscarlos.vulpecula.legacy.bacikal.action.location

import taboolib.common.util.Location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-21 00:18
 */
object ActionLocationModify : ActionLocation.Resolver {

    override val name: Array<String> = arrayOf("modify", "set")

    /**
     * loc modify &loc x y z -world &world ...
     * loc modify &loc -x &x -y &y -z &z -world &world ...
     * */
    override fun resolve(reader: ActionLocation.Reader): ActionLocation.Handler<out Any?> {
        val source = reader.source().accept(reader)

        return reader.transfer {
            if (peekToken().matches("-?\\d+(.\\d+)?".toRegex())) {
                /*
                * 数字
                * loc modify &loc x y z -world &world ...
                * */
                combine(
                    source,
                    double(), // x
                    double(), // y
                    double(), // z
                    argument("world", "w", then = text()),
                    argument("yaw", then = float()),
                    argument("pitch", "p", then = float())
                ) { location, x, y, z, world, yaw, pitch ->
                    if (world != null) {
                        Location(
                            world, x, y, z,
                            yaw ?: location.yaw,
                            pitch ?: location.pitch
                        )
                    } else {
                        location.x = x
                        location.y = y
                        location.z = z
                        location.yaw = yaw ?: location.yaw
                        location.pitch = pitch ?: location.pitch
                        location
                    }
                }
            } else {
                /*
                * 参数
                * loc modify &loc -x &x -y &y -z &z -world &world ...
                * */
                combine(
                    source,
                    argument("x", then = double()),
                    argument("y", then = double()),
                    argument("z", then = double()),
                    argument("world", "w", then = text()),
                    argument("yaw", then = float()),
                    argument("pitch", "p", then = float())
                ) { location, x, y, z, world, yaw, pitch ->
                    if (world != null) {
                        Location(
                            world,
                            x ?: location.x,
                            y ?: location.y,
                            z ?: location.z,
                            yaw ?: location.yaw,
                            pitch ?: location.pitch
                        )
                    } else {
                        location.x = x ?: location.x
                        location.y = y ?: location.y
                        location.z = z ?: location.z
                        location.yaw = yaw ?: location.yaw
                        location.pitch = pitch ?: location.pitch
                        location
                    }
                }
            }
        }
    }
}