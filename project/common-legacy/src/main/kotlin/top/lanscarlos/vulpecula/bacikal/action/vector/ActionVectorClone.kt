package top.lanscarlos.vulpecula.bacikal.action.vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.vector
 *
 * @author Lanscarlos
 * @since 2023-03-22 15:01
 */
object ActionVectorClone : ActionVector.Resolver {

    override val name: Array<String> = arrayOf("clone")

    /**
     * vec clone &vec
     * */
    override fun resolve(reader: ActionVector.Reader): ActionVector.Handler<out Any?> {
        return reader.transfer {
            combine(
                source()
            ) { vector ->
                vector.clone()
            }
        }
    }
}