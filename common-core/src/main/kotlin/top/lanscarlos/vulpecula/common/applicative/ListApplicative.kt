package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 11:15
 */
object ListApplicative : AbstractApplicative<List<*>>(List::class.java) {

    override fun convertOrThrow(instance: Any): List<*> {
        return when (instance) {
            is Array<*> -> instance.toList()
            is Collection<*> -> instance.toList()
            is Map<*, *> -> instance.toList()
            else -> listOf(instance)
        }
    }

}