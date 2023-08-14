package top.lanscarlos.vulpecula.bacikal.action.vector

import taboolib.common.util.Vector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.vector
 *
 * @author Lanscarlos
 * @since 2023-03-22 15:50
 */
object ActionVectorRandom : ActionVector.Resolver {

    override val name: Array<String> = arrayOf("random")

    /**
     * vec random
     * */
    override fun resolve(reader: ActionVector.Reader): ActionVector.Handler<out Any?> {
        return reader.transfer {
            discrete {
                Vector.getRandom()
            }
        }
    }
}