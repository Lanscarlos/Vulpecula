package top.lanscarlos.vulpecula.kether.live

import taboolib.module.kether.ScriptFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * 用于兼容 Kether 语句读取到的不同类型的数据
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:03
 */
interface LiveData<T> {
    fun get(frame: ScriptFrame, def: T): T
}