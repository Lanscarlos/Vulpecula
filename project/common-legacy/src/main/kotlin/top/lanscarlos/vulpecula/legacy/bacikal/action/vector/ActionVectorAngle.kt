package top.lanscarlos.vulpecula.legacy.bacikal.action.vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.vector
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:58
 */
object ActionVectorAngle : ActionVector.Resolver {

    override val name: Array<String> = arrayOf("angle")

    /**
     * vec angle &vec with/by &target
     * */
    override fun resolve(reader: ActionVector.Reader): ActionVector.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
                trim("with", "by", then = vector(display = "vector target"))
            ) { vector, target ->
                vector.angle(target)
            }
        }
    }
}