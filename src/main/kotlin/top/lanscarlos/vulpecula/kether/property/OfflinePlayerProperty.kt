package top.lanscarlos.vulpecula.kether.property

import org.bukkit.OfflinePlayer
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.coerceBoolean

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2023-02-06 14:24
 */
@VulKetherProperty(
    id = "offline-player",
    bind = OfflinePlayer::class
)
class OfflinePlayerProperty : VulScriptProperty<OfflinePlayer>("offline-player") {

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