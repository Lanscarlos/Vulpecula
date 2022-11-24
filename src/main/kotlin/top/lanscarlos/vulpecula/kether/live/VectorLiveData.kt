package top.lanscarlos.vulpecula.kether.live

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
 * @since 2022-11-10 15:17
 */
class VectorLiveData(
    val value: Any
) : LiveData<Vector> {

    override fun get(frame: ScriptFrame, def: Vector): CompletableFuture<Vector> {
        return when (value) {
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? DoubleLiveData)?.getOrNull(frame),
                    (value.second as? DoubleLiveData)?.getOrNull(frame),
                    (value.third as? DoubleLiveData)?.getOrNull(frame),
                ).thenTake().thenApply {
                    Vector(it[0].toDouble(def.x), it[1].toDouble(def.x), it[2].toDouble(def.x))
                }
            }
            else -> {
                val future = if (value is ParsedAction<*>) {
                    frame.run(value)
                } else CompletableFuture.completedFuture(value)

                future.thenApply {
                    when (it) {
                        is Vector -> it
                        is org.bukkit.util.Vector -> Vector(it.x, it.y, it.z)
                        is Location -> it.direction
                        is org.bukkit.Location -> it.toProxyLocation().direction
                        else -> def
                    }
                }
            }
        }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Vector?> {
        return when (value) {
            is Triple<*, *, *> -> {
                listOf(
                    (value.first as? DoubleLiveData)?.getOrNull(frame),
                    (value.second as? DoubleLiveData)?.getOrNull(frame),
                    (value.third as? DoubleLiveData)?.getOrNull(frame),
                ).thenTake().thenApply {
                    Vector(
                        it[0]?.toDouble() ?: return@thenApply null,
                        it[1]?.toDouble() ?: return@thenApply null,
                        it[2]?.toDouble() ?: return@thenApply null
                    )
                }
            }
            else -> {
                val future = if (value is ParsedAction<*>) {
                    frame.run(value)
                } else CompletableFuture.completedFuture(value)

                future.thenApply {
                    when (it) {
                        is Vector -> it
                        is org.bukkit.util.Vector -> Vector(it.x, it.y, it.z)
                        is Location -> it.direction
                        is org.bukkit.Location -> it.toProxyLocation().direction
                        else -> null
                    }
                }
            }
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