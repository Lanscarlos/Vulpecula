package top.lanscarlos.vulpecula.kether.property

import org.bukkit.entity.Entity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.util.Vector
import taboolib.common.OpenResult
import taboolib.module.kether.ScriptProperty
import top.lanscarlos.vulpecula.utils.toBoolean
import top.lanscarlos.vulpecula.utils.toFloat
import top.lanscarlos.vulpecula.utils.toInt

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-10-18 14:32
 */
class EntityProperty(
    id: String
) : ScriptProperty<Entity>(id) {

    override fun read(instance: Entity, key: String): OpenResult {
        val property: Any? = when (key) {
            "boundingBox", "bounding-box" -> instance.boundingBox
            "entityId", "entity-id", "id" -> instance.entityId
            "facing" -> instance.facing
            "fallDistance", "fall-distance" -> instance.fallDistance
            "fireTicks", "fire-ticks" -> instance.fireTicks
            "freezeTicks", "freeze-ticks" -> instance.freezeTicks
            "height" -> instance.height
            "lastDamageCause", "last-damage-cause" -> instance.lastDamageCause
            "location", "loc" -> instance.location
            "maxFireTicks", "max-fire-ticks" -> instance.maxFireTicks
            "maxFreezeTicks", "max-freeze-ticks" -> instance.maxFreezeTicks
            "passenger" -> instance.passenger
            "passengers" -> instance.passengers
            "pistonMoveReaction", "piston-move-reaction", "piston-reaction" -> instance.pistonMoveReaction
            "portalCooldown", "portal-cooldown" -> instance.portalCooldown
            "pose" -> instance.pose
            "scoreboardTags", "scoreboard-tags" -> instance.scoreboardTags
            "server" -> instance.server
            "spawn-category" -> instance.spawnCategory
            "ticks-lived" -> instance.ticksLived
            "type" -> instance.type
            "uniqueId", "unique-id", "uuid" -> instance.uniqueId
            "vehicle" -> instance.vehicle
            "velocity" -> instance.velocity
            "width" -> instance.width
            "world" -> instance.world

            "gravity" -> instance.hasGravity()
            "isCustomNameVisible", "is-custom-name-visible", "custom-name-visible" -> instance.isCustomNameVisible
            "isDead", "dead" -> instance.isDead
            "isEmpty", "empty" -> instance.isEmpty
            "isFrozen", "frozen" -> instance.isFrozen
            "isGlowing", "glowing" -> instance.isGlowing
            "isInsideVehicle", "inside-vehicle" -> instance.isInsideVehicle
            "isInvulnerable", "invulnerable" -> instance.isInvulnerable
            "isInWater", "in-water" -> instance.isInWater
            "isOnGround", "on-ground" -> instance.isOnGround
            "isPersistent", "persistent" -> instance.isPersistent
            "isSilent", "silent" -> instance.isSilent
            "isValid", "valid" -> instance.isValid
            "isVisualFire", "visual-fire" -> instance.isVisualFire

            // Nameable
            "customName", "custom-name" -> instance.customName

            // CommandSender
            "name" -> instance.name

            // ServerOperator
            "isOp", "op" -> instance.isOp
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun write(instance: Entity, key: String, value: Any?): OpenResult {
        when (key) {
            "isCustomNameVisible", "is-custom-name-visible", "custom-name-visible" -> {
                instance.isCustomNameVisible = value?.toBoolean() ?: return OpenResult.failed()
            }
            "fallDistance", "fall-distance" -> {
                instance.fallDistance = value?.toFloat() ?: return OpenResult.failed()
            }
            "fireTicks", "fire-ticks" -> {
                instance.fireTicks = value?.toInt() ?: return OpenResult.failed()
            }
            "freezeTicks", "freeze-ticks" -> {
                instance.freezeTicks = value?.toInt() ?: return OpenResult.failed()
            }
            "isGlowing", "glowing" -> {
                instance.isGlowing = value?.toBoolean() ?: return OpenResult.failed()
            }
            "gravity" -> {
                instance.setGravity(value?.toBoolean() ?: return OpenResult.failed())
            }
            "isInsideVehicle", "inside-vehicle" -> instance.isInsideVehicle
            "isInvulnerable", "invulnerable" -> {
                instance.isInvulnerable = value?.toBoolean() ?: return OpenResult.failed()
            }
            "lastDamageCause", "last-damage-cause" -> {
                instance.lastDamageCause = value as? EntityDamageEvent
            }
            "passenger" -> {
                instance.setPassenger(value as? Entity ?: return OpenResult.failed())
            }
            "isPersistent", "persistent" -> {
                instance.isPersistent = value?.toBoolean() ?: return OpenResult.failed()
            }
            "portalCooldown", "portal-cooldown" -> {
                instance.portalCooldown = value?.toInt() ?: return OpenResult.failed()
            }
            "rotation" -> {
                val pair = value as? Pair<*, *> ?: return OpenResult.failed()
                instance.setRotation(
                    pair.first.toFloat(0f),
                    pair.second.toFloat(0f)
                )
            }
            "isSilent", "silent" -> {
                instance.isSilent = value?.toBoolean() ?: return OpenResult.failed()
            }
            "ticks-lived" -> {
                instance.ticksLived = value?.toInt() ?: return OpenResult.failed()
            }
            "velocity" -> {
                instance.velocity = value as? Vector ?: Vector(0, 0, 0)
            }
            "isVisualFire", "visual-fire" -> {
                instance.isVisualFire = value?.toBoolean() ?: return OpenResult.failed()
            }

            // Nameable
            "customName", "custom-name" -> {
                instance.customName = value?.toString() ?: return OpenResult.failed()
            }

            // ServerOperator
            "isOp", "op" -> {
                instance.isOp = value?.toBoolean() ?: return OpenResult.failed()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}