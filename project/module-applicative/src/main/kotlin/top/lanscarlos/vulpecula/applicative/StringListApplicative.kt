package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:47
 */
object StringListApplicative : ListApplicative<String>() {

    override fun mapping(instance: Any?): String {
        return instance.toString()
    }

}