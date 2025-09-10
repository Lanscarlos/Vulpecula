package top.lanscarlos.vulpecula.module.action.entity

import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.library.xseries.XPotion
import taboolib.module.nms.MinecraftVersion
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.entity
 *
 * @author Lanscarlos
 * @since 2025/7/3
 */
@Parser("entity.potion.size")
object ActionEntityPotionSize : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val entity = ActionEntity.getContext(frame)
        require(entity is LivingEntity) {
            asLang("module-action-entity-exception-entity-unsupported-potion", entity.type.name)
        }
        return entity.activePotionEffects.size
    }

}

@Parser("entity.potion.has")
object ActionEntityPotionHas : ClassActionResolver {

    fun resolve(frame: BacikalFrame, type: String): Boolean {
        val entity = ActionEntity.getContext(frame)
        require(entity is LivingEntity) {
            asLang("module-action-entity-exception-entity-unsupported-potion", entity.type.name)
        }
        val potionEffectType = getPotionEffectType(type)
        return entity.hasPotionEffect(potionEffectType)
    }

}

@Parser("entity.potion.set")
object ActionEntityPotionSet : ClassActionResolver {

    @Suppress("DEPRECATION")
    fun resolve(
        frame: BacikalFrame,
        type: String,
        duration: Int,
        level: Int,
        @Additional(["ambient", "amb", "a"]) ambient: Boolean = false,
        @Additional(["particles", "particle", "p"]) particles: Boolean = true,
        @Additional(["icon", "i"]) icon: Boolean = true
    ): Boolean {
        val entity = ActionEntity.getContext(frame)
        require(entity is LivingEntity) {
            asLang("module-action-entity-exception-entity-unsupported-potion", entity.type.name)
        }
        val potionEffectType = getPotionEffectType(type)
        val amplifier = level - 1
        val potionEffect = try {
            PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon)
        } catch (_: NoSuchMethodError) {
            // 兼容 v1.12.2
            PotionEffect(potionEffectType, duration, amplifier, ambient, particles)
        }
        val exist = entity.hasPotionEffect(potionEffectType)
        entity.addPotionEffect(potionEffect, true)
        return exist
    }

}

@Parser("entity.potion.remove")
object ActionEntityPotionRemove : ClassActionResolver {

    fun resolve(frame: BacikalFrame, type: String): Boolean {
        val entity = ActionEntity.getContext(frame)
        require(entity is LivingEntity) {
            asLang("module-action-entity-exception-entity-unsupported-potion", entity.type.name)
        }
        val potionEffectType = getPotionEffectType(type)
        if (!entity.hasPotionEffect(potionEffectType)) {
            return false
        }
        entity.removePotionEffect(potionEffectType)
        return true
    }

}

@Parser("entity.potion.clear")
object ActionEntityPotionClear : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Boolean {
        val entity = ActionEntity.getContext(frame)
        require(entity is LivingEntity) {
            asLang("module-action-entity-exception-entity-unsupported-potion", entity.type.name)
        }
        if (entity.activePotionEffects.isEmpty()) {
            return false
        }
        for (potionEffect in entity.activePotionEffects) {
            entity.removePotionEffect(potionEffect.type)
        }
        return true
    }

}

private fun getPotionEffectType(type: String): PotionEffectType {
    return getXPotion(type).potionEffectType!!
}

private fun getXPotion(type: String): XPotion {
    val xPotion = XPotion.entries.find { it.name.equals(type, true) }
        ?: error(asLang("module-action-item-exception-invalid-potion-type", type))
    require(xPotion.isSupported) {
        asLang("module-action-item-exception-unsupported-potion-type", MinecraftVersion.runningVersion, type)
    }
    return xPotion
}