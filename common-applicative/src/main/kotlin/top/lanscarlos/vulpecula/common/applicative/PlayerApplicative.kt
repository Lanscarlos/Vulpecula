package top.lanscarlos.vulpecula.common.applicative

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.platform.util.toBukkitLocation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2023-08-21 14:26
 */
object PlayerApplicative : AbstractApplicative<Player>(Player::class.java) {

    override fun convertOrNull(instance: Any?): Player? {
        return when (instance) {
            is Player -> instance
            is OfflinePlayer -> instance.player
            is ProxyPlayer -> instance.castSafely()
            is String -> Bukkit.getPlayerExact(instance)
            else -> null
        }
    }

    override fun readProperty(instance: Player, key: String): Any? {
        return when (key) {
            "name" -> instance.name
            "displayName" -> instance.displayName
            "playerListName" -> instance.playerListName
            "playerListHeader" -> instance.playerListHeader
            "playerListFooter" -> instance.playerListFooter
            "compassTarget" -> instance.compassTarget
            "address" -> instance.address
            "isSneaking" -> instance.isSneaking
            "isSprinting" -> instance.isSprinting
            "isSleepingIgnored" -> instance.isSleepingIgnored
            "bedSpawnLocation" -> instance.bedSpawnLocation
            "gameMode" -> instance.gameMode.name
            "gameMode*" -> instance.gameMode
            "playerTime" -> instance.playerTime
            "playerTimeOffset" -> instance.playerTimeOffset
            "isPlayerTimeRelative" -> instance.isPlayerTimeRelative
            "playerWeather" -> instance.playerWeather?.name
            "playerWeather*" -> instance.playerWeather
            "expCooldown" -> instance.expCooldown
            "exp" -> instance.exp
            "level" -> instance.level
            "totalExperience" -> instance.totalExperience
            "allowFlight" -> instance.allowFlight
            "isFlying" -> instance.isFlying
            "flySpeed" -> instance.flySpeed
            "walkSpeed" -> instance.walkSpeed
            "scoreboard" -> instance.scoreboard
            "worldBorder" -> instance.worldBorder
            "isHealthScaled" -> instance.isHealthScaled
            "healthScale" -> instance.healthScale
            "spectatorTarget" -> instance.spectatorTarget
            "clientViewDistance" -> instance.clientViewDistance
            "ping" -> instance.ping
            "locale" -> instance.locale
            "isAllowingServerListings" -> instance.isAllowingServerListings
            else -> failedByGetPropertyNotSupported(instance, key)
        }
    }

    override fun writeProperty(instance: Player, key: String, value: Any?) {
        when (key) {
            "playerListHeader" -> instance.playerListHeader = value.toString()
            "playerListFooter" -> instance.playerListFooter = value.toString()
            "compassTarget" -> instance.compassTarget = value.applicativeLocation().toBukkitLocation()
            "isSneaking" -> instance.isSneaking = value.applicativeBoolean()
            "isSprinting" -> instance.isSprinting = value.applicativeBoolean()
            "isSleepingIgnored" -> instance.isSleepingIgnored = value.applicativeBoolean()
            "bedSpawnLocation" -> instance.bedSpawnLocation = value.applicativeLocation().toBukkitLocation()
            "gameMode" -> instance.gameMode = GameMode.entries.find { it.name == value.toString() } ?: failedByInvalidValue(instance, key, value)
            "expCooldown" -> instance.expCooldown = value.applicativeInt()
            "exp" -> instance.exp = value.applicativeFloat()
            "level" -> instance.level = value.applicativeInt()
            "totalExperience" -> instance.totalExperience = value.applicativeInt()
            "allowFlight" -> instance.allowFlight = value.applicativeBoolean()
            "isFlying" -> instance.isFlying = value.applicativeBoolean()
            "flySpeed" -> instance.flySpeed = value.applicativeFloat()
            "walkSpeed" -> instance.walkSpeed = value.applicativeFloat()
            "isHealthScaled" -> instance.isHealthScaled = value.applicativeBoolean()
            "healthScale" -> instance.healthScale = value.applicativeDouble()
            else -> failedBySetPropertyNotSupported(instance, key)
        }
    }
}