package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:49
 */
object PrimitiveApplicative {
    
    class BooleanApplicative(source: Any) : AbstractApplicative<Boolean>(source) {
        override fun transfer(source: Any, def: Boolean?): Boolean? {
            return when (source) {
                is Boolean -> source
                "true", "yes" -> true
                "false", "no" -> false
                is Number -> source.toInt() != 0
                is String -> source.toBoolean()
                else -> null
            }
        }
    }

    class ShortApplicative(source: Any) : AbstractApplicative<Short>(source) {
        override fun transfer(source: Any, def: Short?): Short? {
            return when (source) {
                is Number -> source.toShort()
                is String -> source.toShortOrNull()
                else -> def
            }
        }
    }

    class IntApplicative(source: Any) : AbstractApplicative<Int>(source) {
        override fun transfer(source: Any, def: Int?): Int? {
            return when (source) {
                is Number -> source.toInt()
                is String -> source.toIntOrNull()
                else -> def
            }
        }
    }

    class LongApplicative(source: Any) : AbstractApplicative<Long>(source) {
        override fun transfer(source: Any, def: Long?): Long? {
            return when (source) {
                is Number -> source.toLong()
                is String -> source.toLongOrNull()
                else -> def
            }
        }
    }

    class FloatApplicative(source: Any) : AbstractApplicative<Float>(source) {
        override fun transfer(source: Any, def: Float?): Float? {
            return when (source) {
                is Number -> source.toFloat()
                is String -> source.toFloatOrNull()
                else -> def
            }
        }
    }

    class DoubleApplicative(source: Any) : AbstractApplicative<Double>(source) {
        override fun transfer(source: Any, def: Double?): Double? {
            return when (source) {
                is Number -> source.toDouble()
                is String -> source.toDoubleOrNull()
                else -> def
            }
        }
    }

    fun Any.applicativeBoolean() = BooleanApplicative(this)

    fun Any.applicativeShort() = ShortApplicative(this)

    fun Any.applicativeInt() = IntApplicative(this)

    fun Any.applicativeLong() = LongApplicative(this)

    fun Any.applicativeFloat() = FloatApplicative(this)

    fun Any.applicativeDouble() = DoubleApplicative(this)
}