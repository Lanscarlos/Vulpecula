package top.lanscarlos.vulpecula.kether.live

import org.bukkit.entity.Entity
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
 * @since 2022-11-10 15:09
 */
class LocationLiveData(
    val value: Any
) : LiveData<Location> {

    override fun get(frame: ScriptFrame, def: Location): Location {

        val it = if (value is ParsedAction<*>) {
            frame.run(value).join()
        } else value

        return when (it) {
            is Location -> it
            is org.bukkit.Location -> it.toProxyLocation()
            is Entity -> it.location.toProxyLocation()
            is Vector -> Location(def.world, it.x, it.y, it.z)
            is org.bukkit.util.Vector -> Location(def.world, it.x, it.y, it.z)
            is Triple<*, *, *> -> {
                val x = (it.first as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val y = (it.second as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val z = (it.third as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                Location(def.world, x, y, z)
            }
            is Pair<*, *> -> {
                val base = it.first as? Triple<*, *, *> ?: return def
                val meta = it.second as? Pair<*, *> ?: return def
                val x = (base.first as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val y = (base.second as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val z = (base.third as? DoubleLiveData)?.get(frame, def.x) ?: def.x
                val yaw = (meta.first as? DoubleLiveData)?.get(frame, def.yaw.toDouble()) ?: def.yaw
                val pitch = (meta.second as? DoubleLiveData)?.get(frame, def.pitch.toDouble()) ?: def.pitch
                Location(def.world, x, y, z, yaw.toFloat(), pitch.toFloat())
            }
            else -> def
        }
    }

    companion object {

        /**
         *
         * ~ &loc
         * ~ &vec
         *
         * */
        fun read(reader: QuestReader): LiveData<Location> {
//            val value: Any = if (reader.hasNextToken("to")) {
//                reader.nextBlock()
//            } else {
//                val x = reader.readDouble()
//                val y = reader.readDouble()
//                val z = reader.readDouble()
//                if (reader.hasNextToken("with", "and")) {
//                    val yaw = reader.readDouble()
//                    val pitch = reader.readDouble()
//                    Triple(x, y, z) to Pair(yaw, pitch)
//                } else {
//                    Triple(x, y, z)
//                }
//            }
            val value = reader.nextBlock()
            return LocationLiveData(value)
        }
    }
}