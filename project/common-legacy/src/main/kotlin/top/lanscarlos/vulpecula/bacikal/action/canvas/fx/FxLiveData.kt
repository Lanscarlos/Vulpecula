package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveVector

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-30 11:21
 */

val Any.liveNumber: Number?
    get() = when (this) {
        is Number -> this
        is String -> {
            if (this.matches("-?\\d+".toRegex())) {
                // 整数
                this.toIntOrNull() ?: this.toLongOrNull()
            } else if (this.matches("-?\\d+(\\.\\d+)?".toRegex())) {
                // 浮点数
                this.toDoubleOrNull()
            } else {
                null
            }
        }
        else -> null
    }

val Any.liveFxVector: VectorFx<*>?
    get() = when (this) {
        is VectorFx<*> -> this
        is Number -> SimpleVectorFx(this, this, this)
        else -> {
            this.liveVector?.let {
                SimpleVectorFx(it)
            }
        }
    }

fun numberOrNull(): LiveData<Number?> = LiveData.frameBy { it?.liveNumber }

fun number(def: Number? = null, display: String = "number"): LiveData<Number> {
    return LiveData.frameBy { it?.liveNumber ?: def ?: error("No $display selected.") }
}

fun fxNumberOrNull(): LiveData<NumberFx<*, *>?> = LiveData.frameBy { it as? NumberFx<*, *> }

fun fxNumber(def: NumberFx<*, *>? = null, display: String = "fx number"): LiveData<NumberFx<*, *>> {
    return LiveData.frameBy { it as? NumberFx<*, *> ?: def ?: error("No $display selected.") }
}

fun fxVectorOrNull(): LiveData<VectorFx<*>?> = LiveData.frameBy { it?.liveFxVector }

fun fxVector(def: VectorFx<*>? = null, display: String = "fx vector"): LiveData<VectorFx<*>> {
    return LiveData.frameBy { it?.liveFxVector ?: def ?: error("No $display selected.") }
}