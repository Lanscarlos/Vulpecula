package top.lanscarlos.vulpecula.module.property.entity

import org.bukkit.entity.Entity
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.entity
 *
 * @author Lanscarlos
 * @since 2025/09/09
 */
@Property(bind = Entity::class)
object EntityProperty : BacikalProperty<Entity> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Entity, key: String): Any? {
        return try {
            when(key) {
                "location" -> instance.location
                "velocity" -> instance.velocity
                "height" -> instance.height
                "width" -> instance.width
                "boundingBox" -> instance.boundingBox
                "world" -> instance.world
                "entityId" -> instance.entityId
                "fireTicks" -> instance.fireTicks
                "maxFireTicks" -> instance.maxFireTicks
                "freezeTicks" -> instance.freezeTicks
                "maxFreezeTicks" -> instance.maxFreezeTicks
                "server" -> instance.server
                "passenger" -> instance.passenger
                "passengers" -> instance.passengers
                "fallDistance" -> instance.fallDistance
                "lastDamageCause" -> instance.lastDamageCause
                "uniqueId" -> instance.uniqueId
                "ticksLived" -> instance.ticksLived
                "type" -> instance.type
                "swimSound" -> instance.swimSound
                "swimSplashSound" -> instance.swimSplashSound
                "swimHighSpeedSplashSound" -> instance.swimHighSpeedSplashSound
                "vehicle" -> instance.vehicle
                "trackedBy" -> instance.trackedBy
                "portalCooldown" -> instance.portalCooldown
                "scoreboardTags" -> instance.scoreboardTags
                "pistonMoveReaction" -> instance.pistonMoveReaction
                "facing" -> instance.facing
                "pose" -> instance.pose
                "spawnCategory" -> instance.spawnCategory
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Entity, key: String, value: Any?) {
        try {
            when(key) {
                "velocity" -> instance.velocity = value.let(BukkitVectorApplicative::convert)
                "fireTicks" -> instance.fireTicks = value.let(IntApplicative::convert)
                "visualFire" -> instance.isVisualFire = value.let(BooleanApplicative::convert)
                "freezeTicks" -> instance.freezeTicks = value.let(IntApplicative::convert)
                "persistent" -> instance.isPersistent = value.let(BooleanApplicative::convert)
                "passenger" -> instance.setPassenger(value.let(EntityApplicative::convert))
                "fallDistance" -> instance.fallDistance = value.let(FloatApplicative::convert)
                "ticksLived" -> instance.ticksLived = value.let(IntApplicative::convert)
                "customNameVisible" -> instance.isCustomNameVisible = value.let(BooleanApplicative::convert)
                "visibleByDefault" -> instance.isVisibleByDefault = value.let(BooleanApplicative::convert)
                "glowing" -> instance.isGlowing = value.let(BooleanApplicative::convert)
                "invulnerable" -> instance.isInvulnerable = value.let(BooleanApplicative::convert)
                "silent" -> instance.isSilent = value.let(BooleanApplicative::convert)
                "gravity" -> instance.setGravity(value.let(BooleanApplicative::convert))
                "portalCooldown" -> instance.portalCooldown = value.let(IntApplicative::convert)

                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}