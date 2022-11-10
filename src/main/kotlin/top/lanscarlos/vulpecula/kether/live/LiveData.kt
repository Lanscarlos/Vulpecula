package top.lanscarlos.vulpecula.kether.live

import taboolib.module.kether.ScriptFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:03
 */
interface LiveData<T> {
    fun get(frame: ScriptFrame, def: T): T
}