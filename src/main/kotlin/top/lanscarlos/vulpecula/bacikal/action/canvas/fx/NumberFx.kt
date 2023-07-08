package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-30 00:06
 */
abstract class NumberFx<T, R: Number> : Number(), Fx<T, R> {

    override fun toByte(): Byte {
        return calculate().toByte()
    }

    override fun toChar(): Char {
        return calculate().toChar()
    }

    override fun toShort(): Short {
        return calculate().toShort()
    }

    override fun toInt(): Int {
        return calculate().toInt()
    }

    override fun toLong(): Long {
        return calculate().toLong()
    }

    override fun toFloat(): Float {
        return calculate().toFloat()
    }

    override fun toDouble(): Double {
        return calculate().toDouble()
    }

    override fun toString(): String {
        return calculate().toString()
    }
}