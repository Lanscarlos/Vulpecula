package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.live.readItemStack
import top.lanscarlos.vulpecula.kether.live.readLocation
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2023-01-23 23:48
 */
object ItemDropHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("drop")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null

        return if (reader.hasNextToken("to")) {
            acceptHandleNow(source) { item ->
                val loc = playerOrNull()?.toBukkit()?.location ?: error("No location selected.")
                loc.world?.dropItem(loc, item)
            }
        } else {
            val location = reader.readLocation()
            acceptHandleFuture(source) { item ->
                location.getOrNull(this).thenApply {
                    val loc = it?.toBukkitLocation() ?: error("No location selected.")
                    loc.world?.dropItem(loc, item)
                }
            }
        }
    }
}