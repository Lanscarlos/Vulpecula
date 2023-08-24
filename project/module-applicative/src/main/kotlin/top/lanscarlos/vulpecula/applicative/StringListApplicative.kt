package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-25 00:47
 */
class StringListApplicative(source: Any) : AbstractApplicative<List<String>>(source) {

    override fun transfer(source: Any, def: List<String>?): List<String>? {
        return when (source) {
            is String -> listOf(source)
            is Array<*> -> source.map { it.toString() }
            is Collection<*> -> source.map { it.toString() }
            else -> def
        }
    }

    companion object {

        fun Any.applicativeStringList() = StringListApplicative(this)
    }
}