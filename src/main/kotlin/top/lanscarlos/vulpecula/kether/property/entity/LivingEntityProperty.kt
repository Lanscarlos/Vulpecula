package top.lanscarlos.vulpecula.kether.property.entity

import org.bukkit.entity.LivingEntity
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property.entity
 *
 * @author Lanscarlos
 * @since 2022-11-12 15:14
 */

@VulKetherProperty(
    id = "living-entity",
    bind = LivingEntity::class
)
class LivingEntityProperty : VulScriptProperty<LivingEntity>("living-entity") {

    override fun readProperty(instance: LivingEntity, key: String): OpenResult {
        val property: Any? = when (key) {
            "equipment" -> instance.equipment
            "eye-loc" -> instance.eyeLocation
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: LivingEntity, key: String, value: Any?): OpenResult {
        return OpenResult.failed()
    }
}