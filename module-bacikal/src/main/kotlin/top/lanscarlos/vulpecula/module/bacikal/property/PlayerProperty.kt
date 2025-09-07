package top.lanscarlos.vulpecula.module.bacikal.property

import org.bukkit.entity.Player
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/09/07
 */
object PlayerProperty : BacikalProperty<Player> {

    override fun readProperty(instance: Player, key: String): Any? {
        return try {
            when(key) {
                "name" -> instance.getName()
                "displayName" -> instance.getDisplayName()
                "playerListName" -> instance.getPlayerListName()
                "playerListHeader" -> instance.getPlayerListHeader()
                "playerListFooter" -> instance.getPlayerListFooter()
                "compassTarget" -> instance.getCompassTarget()
                "address" -> instance.getAddress()
                "bedSpawnLocation" -> instance.getBedSpawnLocation()
                "previousGameMode" -> instance.getPreviousGameMode()
                "playerTime" -> instance.getPlayerTime()
                "playerTimeOffset" -> instance.getPlayerTimeOffset()
                "playerWeather" -> instance.getPlayerWeather()
                "expCooldown" -> instance.getExpCooldown()
                "exp" -> instance.getExp()
                "level" -> instance.getLevel()
                "totalExperience" -> instance.getTotalExperience()
                "allowFlight" -> instance.getAllowFlight()
                "flySpeed" -> instance.getFlySpeed()
                "walkSpeed" -> instance.getWalkSpeed()
                "scoreboard" -> instance.getScoreboard()
                "worldBorder" -> instance.getWorldBorder()
                "healthScale" -> instance.getHealthScale()
                "spectatorTarget" -> instance.getSpectatorTarget()
                "clientViewDistance" -> instance.getClientViewDistance()
                "ping" -> instance.getPing()
                "locale" -> instance.getLocale()
                "playerProfile" -> instance.getPlayerProfile()
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    override fun writeProperty(instance: Player, key: String, value: Any?) {
        try {
            when(key) {
                "displayName" -> instance.setDisplayName(value.let(StringApplicative::convert))
                "playerListName" -> instance.setPlayerListName(value.let(StringApplicative::convert))
                "playerListHeader" -> instance.setPlayerListHeader(value.let(StringApplicative::convert))
                "playerListFooter" -> instance.setPlayerListFooter(value.let(StringApplicative::convert))
                "sneaking" -> instance.setSneaking(value.let(BooleanApplicative::convert))
                "sprinting" -> instance.setSprinting(value.let(BooleanApplicative::convert))
                "sleepingIgnored" -> instance.setSleepingIgnored(value.let(BooleanApplicative::convert))
                "expCooldown" -> instance.setExpCooldown(value.let(IntApplicative::convert))
                "exp" -> instance.setExp(value.let(FloatApplicative::convert))
                "level" -> instance.setLevel(value.let(IntApplicative::convert))
                "totalExperience" -> instance.setTotalExperience(value.let(IntApplicative::convert))
                "allowFlight" -> instance.setAllowFlight(value.let(BooleanApplicative::convert))
                "flying" -> instance.setFlying(value.let(BooleanApplicative::convert))
                "flySpeed" -> instance.setFlySpeed(value.let(FloatApplicative::convert))
                "walkSpeed" -> instance.setWalkSpeed(value.let(FloatApplicative::convert))
                "texturePack" -> instance.setTexturePack(value.let(StringApplicative::convert))
                "resourcePack" -> instance.setResourcePack(value.let(StringApplicative::convert))
                "healthScaled" -> instance.setHealthScaled(value.let(BooleanApplicative::convert))
                "healthScale" -> instance.setHealthScale(value.let(DoubleApplicative::convert))
                "spectatorTarget" -> instance.setSpectatorTarget(value.let(EntityApplicative::convert))
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}