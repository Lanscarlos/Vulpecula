package top.lanscarlos.vulpecula.kether.live

import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-10 15:17
 */
class VectorLiveData(
    val value: Any
) : LiveData<Vector> {

    override fun get(frame: ScriptFrame, def: Vector): Vector {

        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is Vector -> it
            is org.bukkit.util.Vector -> Vector(it.x, it.y, it.z)
            is Location -> it.direction
            is org.bukkit.Location -> it.toProxyLocation().direction
            is Triple<*, *, *> -> {
                val x = (it.first as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val y = (it.second as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val z = (it.third as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                Vector(x, y, z)
            }
            else -> def
        }
    }

    override fun getOrNull(frame: ScriptFrame): Vector? {
        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is Vector -> it
            is org.bukkit.util.Vector -> Vector(it.x, it.y, it.z)
            is Location -> it.direction
            is org.bukkit.Location -> it.toProxyLocation().direction
            is Triple<*, *, *> -> {
                val x = (it.first as? DoubleLiveData)?.getOrNull(frame) ?: return null
                val y = (it.second as? DoubleLiveData)?.getOrNull(frame) ?: return null
                val z = (it.third as? DoubleLiveData)?.getOrNull(frame) ?: return null
                Vector(x, y, z)
            }
            else -> null
        }
    }

    companion object {

        /**
         * 读取 Vector 对象
         * @param produce 是否通过参数构建对象
         * */
        fun read(reader: QuestReader, produce: Boolean): LiveData<Vector> {
            val value: Any = if (produce) {
                val x = reader.readDouble()
                val y = reader.readDouble()
                val z = reader.readDouble()
                Triple(x, y, z)
            } else {
                reader.nextBlock()
            }
            return VectorLiveData(value)
        }
    }
}