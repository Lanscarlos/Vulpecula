package top.lanscarlos.vulpecula.bacikal.action.location

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.location
 *
 * @author Lanscarlos
 * @since 2023-03-21 15:01
 */
object ActionLocationMultiply : ActionLocation.Resolver {

    override val name: Array<String> = arrayOf("mul", "times")

    /**
     * loc mul &loc with &target
     * loc mul &loc with x,y,z
     * loc mul &loc &x &y &z -yaw &yaw ...
     * loc mul &loc -x &x -y &y -z &z -yaw &yaw ...
     * */
    override fun resolve(reader: ActionLocation.Reader): ActionLocation.Handler<out Any?> {
        val source = reader.source().accept(reader)

        return reader.transfer {
            if (expectToken("with", "by")) {
                /*
                * 坐标对象
                * loc mul &loc with &target
                * loc mul &loc with 0,0,0
                * */
                combine(
                    source,
                    location(display = "location target")
                ) { location, target ->
                    location.x *= target.x
                    location.y *= target.y
                    location.z *= target.z
                    location.yaw *= target.yaw
                    location.pitch *= target.pitch
                    location
                }
            } else if (peekToken().matches("-?\\d+(.\\d+)?".toRegex())) {
                /*
                * 数字
                * loc mul &loc x y z -world &world ...
                * */
                combine(
                    source,
                    double(1.0), // x
                    double(1.0), // y
                    double(1.0), // z
                    argument("yaw", then = float(), def = 1f),
                    argument("pitch", "p", then = float(), def = 1f)
                ) { location, x, y, z, yaw, pitch ->
                    location.x *= x
                    location.y *= y
                    location.z *= z
                    location.yaw *= yaw
                    location.pitch *= pitch
                    location
                }
            } else {
                /*
                * 参数
                * loc mul &loc -x &x -y &y -z &z -world &world ...
                * */
                combine(
                    source,
                    argument("x", then = double(), def = 1.0),
                    argument("y", then = double(), def = 1.0),
                    argument("z", then = double(), def = 1.0),
                    argument("yaw", then = float(), def = 1f),
                    argument("pitch", "p", then = float(), def = 1f)
                ) { location, x, y, z, yaw, pitch ->
                    location.x *= x
                    location.y *= y
                    location.z *= z
                    location.yaw *= yaw
                    location.pitch *= pitch
                    location
                }
            }
        }
    }
}