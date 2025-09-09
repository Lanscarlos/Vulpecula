package top.lanscarlos.vulpecula.module.property.entity

import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/09/09
 */
object PlayerProperty : BacikalProperty<Player> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Player, key: String): Any? {
        return try {
            when(key) {
                "name" -> instance.name
                "displayName" -> instance.displayName
                "playerListName" -> instance.playerListName
                "playerListOrder" -> instance.playerListOrder
                "playerListHeader" -> instance.playerListHeader
                "playerListFooter" -> instance.playerListFooter
                "compassTarget" -> instance.compassTarget
                "address" -> instance.address
                "bedSpawnLocation" -> instance.bedSpawnLocation
                "respawnLocation" -> instance.respawnLocation
                "previousGameMode" -> instance.previousGameMode
                "playerTime" -> instance.playerTime
                "playerTimeOffset" -> instance.playerTimeOffset
                "playerWeather" -> instance.playerWeather
                "expCooldown" -> instance.expCooldown
                "exp" -> instance.exp
                "level" -> instance.level
                "totalExperience" -> instance.totalExperience
                "allowFlight" -> instance.allowFlight
                "flySpeed" -> instance.flySpeed
                "walkSpeed" -> instance.walkSpeed
                "scoreboard" -> instance.scoreboard
                "worldBorder" -> instance.worldBorder
                "healthScale" -> instance.healthScale
                "spectatorTarget" -> instance.spectatorTarget
                "clientViewDistance" -> instance.clientViewDistance
                "ping" -> instance.ping
                "locale" -> instance.locale
                "playerProfile" -> instance.playerProfile
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Player, key: String, value: Any?) {
        try {
            when(key) {
                "displayName" -> instance.setDisplayName(value.let(StringApplicative::convert))
                "playerListName" -> instance.setPlayerListName(value.let(StringApplicative::convert))
                "playerListOrder" -> instance.playerListOrder = value.let(IntApplicative::convert)
                "playerListHeader" -> instance.playerListHeader = value.let(StringApplicative::convert)
                "playerListFooter" -> instance.playerListFooter = value.let(StringApplicative::convert)
                "compassTarget" -> instance.compassTarget = value.let(BukkitLocationApplicative::convert)
                "sneaking" -> instance.isSneaking = value.let(BooleanApplicative::convert)
                "sprinting" -> instance.isSprinting = value.let(BooleanApplicative::convert)
                "sleepingIgnored" -> instance.isSleepingIgnored = value.let(BooleanApplicative::convert)
                "bedSpawnLocation" -> instance.bedSpawnLocation = value.let(BukkitLocationApplicative::convert)
                "respawnLocation" -> instance.respawnLocation = value.let(BukkitLocationApplicative::convert)
                "expCooldown" -> instance.expCooldown = value.let(IntApplicative::convert)
                "exp" -> instance.exp = value.let(FloatApplicative::convert)
                "level" -> instance.level = value.let(IntApplicative::convert)
                "totalExperience" -> instance.totalExperience = value.let(IntApplicative::convert)
                "allowFlight" -> instance.allowFlight = value.let(BooleanApplicative::convert)
                "flying" -> instance.isFlying = value.let(BooleanApplicative::convert)
                "flySpeed" -> instance.flySpeed = value.let(FloatApplicative::convert)
                "walkSpeed" -> instance.walkSpeed = value.let(FloatApplicative::convert)
                "texturePack" -> instance.setTexturePack(value.let(StringApplicative::convert))
                "resourcePack" -> instance.setResourcePack(value.let(StringApplicative::convert))
                "healthScaled" -> instance.isHealthScaled = value.let(BooleanApplicative::convert)
                "healthScale" -> instance.healthScale = value.let(DoubleApplicative::convert)
                "spectatorTarget" -> instance.spectatorTarget = value.let(EntityApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}