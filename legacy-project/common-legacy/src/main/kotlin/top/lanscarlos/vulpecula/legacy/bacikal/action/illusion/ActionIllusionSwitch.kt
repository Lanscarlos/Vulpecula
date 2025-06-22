package top.lanscarlos.vulpecula.legacy.bacikal.action.illusion

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.legacy.bacikal.Bacikal
import top.lanscarlos.vulpecula.legacy.utils.setVariable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.illusion
 *
 * @author Lanscarlos
 * @since 2023-08-10 17:58
 */
object ActionIllusionSwitch : ActionIllusion.Resolver {

    override val name = arrayOf("switch")

    override fun resolve(reader: ActionIllusion.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                any(),
            ) { source ->
                val targets: Collection<Player> = when (source) {
                    "*" -> {
                        Bukkit.getOnlinePlayers()
                    }
                    is Player -> {
                        listOf(source)
                    }
                    is BukkitPlayer -> {
                        listOf(source.player)
                    }
                    is OfflinePlayer -> {
                        source.player?.let { listOf(it) } ?: return@combine false
                    }
                    is String -> {
                        Bukkit.getPlayerExact(source)?.let { listOf(it) } ?: return@combine false
                    }
                    is List<*> -> {
                        source.mapNotNull {
                            when (it) {
                                is Player -> it
                                is BukkitPlayer -> it.player
                                is String -> Bukkit.getPlayerExact(it)
                                is OfflinePlayer -> it.player
                                else -> null
                            }
                        }
                    }
                    else -> return@combine false
                }

                this.setVariable("@Hallucinators", targets.distinct())
                return@combine true
            }
        }
    }
}