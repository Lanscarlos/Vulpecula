package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.OfflinePlayer
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.common
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
@Property(bind = OfflinePlayer::class)
object OfflinePlayerProperty : BacikalProperty<OfflinePlayer> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: OfflinePlayer, key: String): Any? {
        return try {
            when(key) {
                "name" -> instance.name
                "uniqueId" -> instance.uniqueId
                "playerProfile" -> instance.playerProfile
                "player" -> instance.player
                "firstPlayed" -> instance.firstPlayed
                "lastPlayed" -> instance.lastPlayed
                "bedSpawnLocation" -> instance.bedSpawnLocation
                "respawnLocation" -> instance.respawnLocation
                "lastDeathLocation" -> instance.lastDeathLocation
                "location" -> instance.location
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: OfflinePlayer, key: String, value: Any?) {
        try {
            when(key) {
                "whitelisted" -> instance.isWhitelisted = value.let(BooleanApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}