package top.lanscarlos.vulpecula.kether.action.location

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.LocationLiveData
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.kether.live.readLocation
import top.lanscarlos.vulpecula.utils.playerOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.location
 *
 * @author Lanscarlos
 * @since 2022-11-26 11:26
 */
object LocationDistanceHandler : ActionLocation.Reader {

    override val name: Array<String> = arrayOf("distance", "dist", "dist-sq")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionLocation.Handler {
        val source = if (isRoot) reader.readLocation() else null
        val other = reader.expectVector()

        return acceptHandleFuture(source) { location ->
            val base = this.playerOrNull()?.location
            val target = if (base != null) other.get(this, base) else other.getOrNull(this)

            target.thenApply {
                if (it == null) return@thenApply -1
                when (input) {
                    "distance", "dist" -> location.distance(it)
                    "dist-sq" -> location.distanceSquared(it)
                    else -> -1
                }
            }
        }
    }

    /**
     * 读取期望 Location 数据
     * 主要用于运算
     * */
    private fun QuestReader.expectVector(): LiveData<Location> {
        return if (this.hasNextToken("with")) {
            this.readLocation()
        } else {
            this.expect("using")
            val x = this.readDouble()
            val y = this.readDouble()
            val z = this.readDouble()
            LocationLiveData(Triple(x, y, z))
        }
    }
}