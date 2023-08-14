package top.lanscarlos.vulpecula.bacikal.property

import org.bukkit.OfflinePlayer
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalGenericProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:01
 */
@BacikalProperty(
    id = "offline-player",
    bind = OfflinePlayer::class
)
class OfflinePlayerProperty : BacikalGenericProperty<OfflinePlayer>("offline-player") {

    override fun readProperty(instance: OfflinePlayer, key: String): OpenResult {
        val property: Any? = when (key) {
            "name" -> instance.name
            "bed-location", "bed-loc", "bed" -> instance.bedSpawnLocation
            "first-played" -> instance.firstPlayed
            "last-played" -> instance.lastPlayed
            "player", "entity" -> instance.player
            "profile" -> instance.playerProfile
            "uuid" -> instance.uniqueId.toString()
            "has-played-before", "is-first-play" -> instance.hasPlayedBefore()
            "banned" -> instance.isBanned
            "online" -> instance.isOnline
            "in-white-listed", "in-white-list" -> instance.isWhitelisted
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: OfflinePlayer, key: String, value: Any?): OpenResult {
        when (key) {
            "in-white-listed", "in-white-list" -> {
                instance.isWhitelisted = value?.coerceBoolean() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }

}