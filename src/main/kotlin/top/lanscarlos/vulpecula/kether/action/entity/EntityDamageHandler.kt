package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.readDouble
import top.lanscarlos.vulpecula.kether.live.tryReadEntity
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:09
 */
object EntityDamageHandler : ActionEntity.Reader {

    override val name: Array<String> = arrayOf("damage", "dmg")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionEntity.Handler {
        val source = reader.source(isRoot)
        val damage = reader.readDouble()
        val damager = reader.tryReadEntity("by")

        return acceptTransferFuture(source) { entity ->
            listOf(
                damage.getOrNull(this),
                damager?.getOrNull(this)
            ).thenTake().thenApply {
                (entity as? LivingEntity)?.damage(it[0].coerceDouble(0.0), it[1] as? Entity)
                return@thenApply entity
            }
        }
    }
}