package top.lanscarlos.vulpecula.kether.action

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Location
import taboolib.library.kether.ParsedAction
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

        val res = resource.getOrNull(frame) ?: error("No sound resource selected.")
        val volume = meta.first.get(frame, 1.0).toFloat()
        val pitch = meta.second.get(frame, 1.0).toFloat()

        if (global) {
            val loc = location?.getOrNull(frame)?.toBukkitLocation() ?: error("No location selected.")
            if (res.startsWith("resource:")) {
                loc.world?.playSound(loc, res.substring("resource:".length), volume, pitch)
            } else {
                loc.world?.playSound(loc, Sound.valueOf(res.replace('.', '_').uppercase()), volume, pitch)
            }
        } else {
            val player = target?.getOrNull(frame)?.let {
                if (it is Player) adaptPlayer(it) else null
            } ?: frame.unsafePlayer() ?: error("No player selected.")

            val loc = location.getValue(frame, player.location)
            if (res.startsWith("resource:")) {
                player.playSoundResource(loc, res.substring("resource:".length), volume, pitch)
            } else {
                player.playSound(loc, res.replace('.', '_').uppercase(), volume, pitch)
            }
        }

        return CompletableFuture.completedFuture(false)
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