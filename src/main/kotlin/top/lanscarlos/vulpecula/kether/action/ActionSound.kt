package top.lanscarlos.vulpecula.kether.action

import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.module.kether.*
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.DoubleLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-10-22 09:24
 */
class ActionSound(
    val resource: LiveData<String>,
    val meta: Pair<LiveData<Double>, LiveData<Double>>,
    val target: LiveData<Entity>?,
    val location: LiveData<Location>?,
    val global: Boolean
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        if (global) {
            return resource.thenApplyOrNull(frame,
                location?.getOrNull(frame) ?: error("No location selected."),
                meta.first.getOrNull(frame), // volume
                meta.second.getOrNull(frame) // pitch
            ) {
                val resource = this ?: error("No sound resource selected.")
                val loc = (it[0] as? Location)?.toBukkitLocation() ?: error("No location selected.")
                val volume = it[1].toFloat(1f)
                val pitch = it[2].toFloat(1f)

                if (resource.startsWith("resource:")) {
                    loc.world?.playSound(loc, resource.substring("resource:".length), volume, pitch)
                } else {
                    loc.world?.playSound(loc, Sound.valueOf(resource.replace('.', '_').uppercase()), volume, pitch)
                }
            }
        } else {
            return resource.thenApplyOrNull(frame,
                target?.getOrNull(frame) ?: CompletableFuture.completedFuture(null),
                location?.getOrNull(frame) ?: CompletableFuture.completedFuture(null),
                meta.first.getOrNull(frame), // volume
                meta.second.getOrNull(frame) // pitch
            ) {
                val resource = this ?: error("No sound resource selected.")
                val player = (it[0] as? Player)?.let { bukkit -> adaptPlayer(bukkit) } ?: frame.unsafePlayer() ?: error("No player selected.")
                val loc = (it[1] as? Location) ?: player.location
                val volume = it[2].toFloat(1f)
                val pitch = it[3].toFloat(1f)

                if (resource.startsWith("resource:")) {
                    player.playSoundResource(loc, resource.substring("resource:".length), volume, pitch)
                } else {
                    player.playSound(loc, resource.replace('.', '_').uppercase(), volume, pitch)
                }
            }
        }
    }

    companion object {
        /**
         * sound* {resource}
         * sound* {resource} by {volume} {pitch}
         * sound* {resource} by {volume} {pitch} at {location} global
         * sound* {resource} by {volume} {pitch} at {location} to {player}
         * */
        @VulKetherParser(
            id = "sound",
            name = ["sound*"],
            override = ["sound"]
        )
        fun parser() = scriptParser { reader ->

            // 解析资源名
            val resource = reader.readString()
            val meta = if (reader.hasNextToken("by", "with")) {
                reader.readDouble() to reader.readDouble()
            } else {
                DoubleLiveData(1.0) to DoubleLiveData(1.0)
            }
            val target = if (reader.hasNextToken("to")) reader.readEntity() else null
            val loc = if (reader.hasNextToken("at")) reader.readLocation() else null
            val global = reader.hasNextToken("global", "-global", "--global")

            ActionSound(resource, meta, target, loc, global)
        }
    }
}