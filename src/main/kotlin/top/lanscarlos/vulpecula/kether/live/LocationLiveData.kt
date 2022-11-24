package top.lanscarlos.vulpecula.kether.live

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

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

    override fun get(frame: ScriptFrame, def: Location): CompletableFuture<Location> {
        return when (value) {
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? DoubleLiveData)?.getOrNull(frame),
                    (value.second as? DoubleLiveData)?.getOrNull(frame),
                    (value.third as? DoubleLiveData)?.getOrNull(frame),
                ).thenTake().thenApply {
                    Location(def.world,
                        it[0].toDouble(def.x),
                        it[1].toDouble(def.y),
                        it[2].toDouble(def.z)
                    )
                }
            }
            is Pair<*, *> -> {
                val base = value.first as? Triple<*, *, *> ?: return CompletableFuture.completedFuture(def)
                val meta = value.second as? Pair<*, *> ?: return CompletableFuture.completedFuture(def)
                listOf(
                    (base.first as? DoubleLiveData)?.getOrNull(frame),
                    (base.second as? DoubleLiveData)?.getOrNull(frame),
                    (base.third as? DoubleLiveData)?.getOrNull(frame),
                    (meta.first as? DoubleLiveData)?.getOrNull(frame),
                    (meta.second as? DoubleLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Location(def.world,
                        it[0].toDouble(def.x),
                        it[1].toDouble(def.y),
                        it[2].toDouble(def.z),
                        it[3].toFloat(def.yaw),
                        it[4].toFloat(def.pitch)
                    )
                }
            }
            else -> {
                val future = if (value is ParsedAction<*>) {
                    frame.run(value)
                } else CompletableFuture.completedFuture(value)

                return future.thenApply {
                    when (it) {
                        is Location -> it
                        is org.bukkit.Location -> it.toProxyLocation()
                        is ProxyPlayer -> it.location
                        is Entity -> it.location.toProxyLocation()
                        is Vector -> Location(def.world, it.x, it.y, it.z)
                        is org.bukkit.util.Vector -> Location(def.world, it.x, it.y, it.z)
                        else -> def
                    }
                }
            }
        }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Location?> {

        return when (value) {
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? DoubleLiveData)?.getOrNull(frame),
                    (value.second as? DoubleLiveData)?.getOrNull(frame),
                    (value.third as? DoubleLiveData)?.getOrNull(frame),
                ).thenTake().thenApply {
                    Location(null,
                        it[0]?.toDouble() ?: return@thenApply null,
                        it[1]?.toDouble() ?: return@thenApply null,
                        it[2]?.toDouble() ?: return@thenApply null
                    )
                }
            }
            is Pair<*, *> -> {
                val base = value.first as? Triple<*, *, *> ?: return CompletableFuture.completedFuture(null)
                val meta = value.second as? Pair<*, *> ?: return CompletableFuture.completedFuture(null)
                listOf(
                    (base.first as? DoubleLiveData)?.getOrNull(frame),
                    (base.second as? DoubleLiveData)?.getOrNull(frame),
                    (base.third as? DoubleLiveData)?.getOrNull(frame),
                    (meta.first as? DoubleLiveData)?.getOrNull(frame),
                    (meta.second as? DoubleLiveData)?.getOrNull(frame)
                ).thenTake().thenApply {
                    Location(null,
                        it[0]?.toDouble() ?: return@thenApply null,
                        it[1]?.toDouble() ?: return@thenApply null,
                        it[2]?.toDouble() ?: return@thenApply null,
                        it[3]?.toFloat() ?: return@thenApply null,
                        it[4]?.toFloat() ?: return@thenApply null
                    )
                }
            }
            else -> {
                val future = if (value is ParsedAction<*>) {
                    frame.run(value)
                } else CompletableFuture.completedFuture(value)

                future.thenApply {
                    when (it) {
                        is Location -> it
                        is org.bukkit.Location -> it.toProxyLocation()
                        is ProxyPlayer -> it.location
                        is Entity -> it.location.toProxyLocation()
                        is Vector -> Location(null, it.x, it.y, it.z)
                        is org.bukkit.util.Vector -> Location(null, it.x, it.y, it.z)
                        else -> null
                    }
                }
            }
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