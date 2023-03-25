package top.lanscarlos.vulpecula.bacikal.action

import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.module.kether.player
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-03-26 00:11
 */
object ActionSound {

    @BacikalParser(
        id = "sound",
        name = ["sound*"],
        override = ["sound"]
    )
    fun parser() = bacikal {
        combine(
            text(display = "sound resource"),
            optional("by", then = float(1f).union(float(1f)), def = 1f to 1f),
            optional("at", then = location()),
            optional("-global", "global", then = LiveData.point(true), def = false),
            optional("to", then = player())
        ) { resource, meta, center, global, player ->
            val volume = meta.first
            val pitch = meta.first

            if (player != null) {
                playSound(adaptPlayer(player), resource, volume, pitch, center)
            } else if (global) {
                onlinePlayers().forEach { playSound(it, resource, volume, pitch, center) }
            } else {
                playSound(this.player(), resource, volume, pitch, center)
            }
        }
    }

    private fun playSound(player: ProxyPlayer, resource: String, volume: Float, pitch: Float, center: Location?) {
        val loc = center ?: player.location
        if (resource.startsWith("resource:")) {
            player.playSoundResource(loc, resource.substring("resource:".length), volume, pitch)
        } else {
            player.playSound(loc, resource.replace('.', '_').uppercase(), volume, pitch)
        }
    }
}