package top.lanscarlos.vulpecula.module.property.entity

import org.bukkit.entity.LivingEntity
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/09/09
 */
object LivingEntityProperty : BacikalProperty<LivingEntity> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: LivingEntity, key: String): Any? {
        return try {
            when(key) {
                "eyeHeight" -> instance.eyeHeight
                "eyeLocation" -> instance.eyeLocation
                "remainingAir" -> instance.remainingAir
                "maximumAir" -> instance.maximumAir
                "itemInUse" -> instance.itemInUse
                "itemInUseTicks" -> instance.itemInUseTicks
                "arrowCooldown" -> instance.arrowCooldown
                "arrowsInBody" -> instance.arrowsInBody
                "maximumNoDamageTicks" -> instance.maximumNoDamageTicks
                "lastDamage" -> instance.lastDamage
                "noDamageTicks" -> instance.noDamageTicks
                "noActionTicks" -> instance.noActionTicks
                "killer" -> instance.killer
                "activePotionEffects" -> instance.activePotionEffects
                "removeWhenFarAway" -> instance.removeWhenFarAway
                "equipment" -> instance.equipment
                "canPickupItems" -> instance.canPickupItems
                "leashHolder" -> instance.leashHolder
                "collidableExemptions" -> instance.collidableExemptions
                "hurtSound" -> instance.hurtSound
                "deathSound" -> instance.deathSound
                "fallDamageSoundSmall" -> instance.fallDamageSoundSmall
                "fallDamageSoundBig" -> instance.fallDamageSoundBig
                "category" -> instance.category
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: LivingEntity, key: String, value: Any?) {
        try {
            when(key) {
                "remainingAir" -> instance.remainingAir = value.let(IntApplicative::convert)
                "maximumAir" -> instance.maximumAir = value.let(IntApplicative::convert)
                "itemInUseTicks" -> instance.itemInUseTicks = value.let(IntApplicative::convert)
                "arrowCooldown" -> instance.arrowCooldown = value.let(IntApplicative::convert)
                "arrowsInBody" -> instance.arrowsInBody = value.let(IntApplicative::convert)
                "maximumNoDamageTicks" -> instance.maximumNoDamageTicks = value.let(IntApplicative::convert)
                "lastDamage" -> instance.lastDamage = value.let(DoubleApplicative::convert)
                "noDamageTicks" -> instance.noDamageTicks = value.let(IntApplicative::convert)
                "noActionTicks" -> instance.noActionTicks = value.let(IntApplicative::convert)
                "removeWhenFarAway" -> instance.removeWhenFarAway = value.let(BooleanApplicative::convert)
                "canPickupItems" -> instance.canPickupItems = value.let(BooleanApplicative::convert)
                "leashHolder" -> instance.setLeashHolder(value?.let(EntityApplicative::convert))
                "gliding" -> instance.isGliding = value.let(BooleanApplicative::convert)
                "swimming" -> instance.isSwimming = value.let(BooleanApplicative::convert)
                "riptiding" -> instance.isRiptiding = value.let(BooleanApplicative::convert)
                "aI" -> instance.setAI(value.let(BooleanApplicative::convert))
                "collidable" -> instance.isCollidable = value.let(BooleanApplicative::convert)
                "invisible" -> instance.isInvisible = value.let(BooleanApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}