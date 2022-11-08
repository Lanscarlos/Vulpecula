package top.lanscarlos.vulpecula.kether.action

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.module.kether.actionNow
import taboolib.module.kether.player
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.run
import top.lanscarlos.vulpecula.utils.tryNextAction
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-10-22 09:24
 */
object ActionSound {

    /**
     * sound* {resource}
     * sound* {resource} by {volume} {pitch}
     * sound* {resource} by {volume} {pitch} at {location} global
     * */
    @VulKetherParser(
        id = "sound",
        name = ["sound*"],
        override = ["sound"]
    )
    fun parser() = scriptParser { reader ->
        // 解析资源名
        val resource = reader.nextToken()

        // 解析音量和音调
        reader.mark()
        val meta = if (reader.hasNextToken("by", "with")) {
            reader.nextDouble().toFloat() to reader.nextDouble().toFloat()
        } else {
            1f to 1f
        }

        val locAction = reader.tryNextAction("at")
        val global = reader.hasNextToken("global", "-global", "--global")

        actionNow {
            if (global) {
                val loc = locAction?.run(this) as? Location ?: error("No location selected.")
                if (resource.startsWith("resource:")) {
                    loc.world?.playSound(loc, resource.substring("resource:".length), meta.first, meta.second)
                } else {
                    loc.world?.playSound(loc, Sound.valueOf(resource.replace('.', '_').uppercase()), meta.first, meta.second)
                }
            } else {
                val player = (this.player() as? BukkitPlayer)?.player ?: error("No player selected.")
                val loc = locAction?.run(this) as? Location ?: player.location
                if (resource.startsWith("resource:")) {
                    player.playSound(loc, resource.substring("resource:".length), meta.first, meta.second)
                } else {
                    player.playSound(loc, Sound.valueOf(resource.replace('.', '_').uppercase()), meta.first, meta.second)
                }
            }
            return@actionNow null
        }
    }
}