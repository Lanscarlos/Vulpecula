package top.lanscarlos.vulpecula.kether.action.entity

import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.utils.readDouble
import top.lanscarlos.vulpecula.utils.readEntity
import top.lanscarlos.vulpecula.utils.tryReadEntity

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
        val source = if (isRoot) reader.readEntity() else null
        val damage = reader.readDouble()
        val damager = reader.tryReadEntity("by")

        return applyLivingEntity(source) { entity ->
            entity.also { it.damage(damage.get(this, 0.0), damager?.getOrNull(this)) }
        }
    }
}