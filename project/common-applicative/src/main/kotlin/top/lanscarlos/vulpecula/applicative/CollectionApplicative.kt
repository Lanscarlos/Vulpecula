package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:35
 */
class CollectionApplicative<T>(val applicative: AbstractApplicative<T>) :
    AbstractApplicative<Collection<T>>(applicative.source) {

    override fun transfer(source: Any, def: Collection<T>?): Collection<T> {
        val defValue = def?.firstOrNull()
        when (source) {
            is Collection<*> -> {
                return source.mapNotNull { applicative.transfer(it ?: return@mapNotNull null, defValue) }
            }

            is Array<*> -> {
                return source.mapNotNull { applicative.transfer(it ?: return@mapNotNull null, defValue) }
            }

            else -> {
                return applicative.transfer(source, defValue)?.let { listOf(it) } ?: emptyList()
            }
        }
    }

    companion object {
        fun <T> Applicative<T>.collection() = CollectionApplicative(
            this as? AbstractApplicative<T>
                ?: throw IllegalArgumentException("Collection Applicative Only Support AbstractApplicative")
        )
    }
}