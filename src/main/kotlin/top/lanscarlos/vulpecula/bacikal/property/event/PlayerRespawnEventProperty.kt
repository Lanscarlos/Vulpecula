package top.lanscarlos.vulpecula.bacikal.property.event

import org.bukkit.Location
import org.bukkit.event.player.PlayerRespawnEvent
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.bacikal.BacikalScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property.event
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:26
 */
@BacikalProperty(
    id = "player-respawn-event",
    bind = PlayerRespawnEvent::class
)
class PlayerRespawnEventProperty : BacikalScriptProperty<PlayerRespawnEvent>("player-respawn-event") {

    override fun readProperty(instance: PlayerRespawnEvent, key: String): OpenResult {
        val property: Any = when (key) {
            "respawn-location", "respawn-loc", "location", "loc" -> instance.respawnLocation
            "use-anchor", "anchor" -> instance.isAnchorSpawn
            "use-bed", "bed" -> instance.isBedSpawn
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: PlayerRespawnEvent, key: String, value: Any?): OpenResult {
        when (key) {
            "respawn-location", "respawn-loc", "location", "loc" -> {
                instance.respawnLocation = value as? Location ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}