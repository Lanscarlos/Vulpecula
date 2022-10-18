package top.lanscarlos.vulpecula.kether.property

import org.bukkit.entity.Entity
import taboolib.module.kether.KetherProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-10-18 00:13
 */
object PropertyRegister {
    @KetherProperty(bind = Entity::class, shared = true)
    fun propertyEntity() = EntityProperty("entity.operator")
}