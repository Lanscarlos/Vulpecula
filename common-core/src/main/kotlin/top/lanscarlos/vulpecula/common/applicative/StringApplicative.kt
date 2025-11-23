package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-11-23 15:44
 */
object StringApplicative : AbstractApplicative<String>(String::class.java) {

    override fun convertOrThrow(instance: Any): String {
        if (instance is Collection<*>) {
            return instance.joinToString("\n") { it.toString() }
        }
        return instance.toString()
    }

}