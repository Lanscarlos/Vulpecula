package top.lanscarlos.vulpecula.bacikal.action.target.selector

import org.bukkit.Bukkit
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.selector
 *
 * @author Lanscarlos
 * @since 2023-03-22 21:46
 */
object ActionTargetSelectPlayer : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("Player")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combine(
                source(),
                text(display = "player name")
            ) { target, name ->
                val offline = Bukkit.getOfflinePlayers().firstOrNull { it.name == name }
                if (offline != null) {
                    target += offline.player ?: offline
                }
                target
            }
        }
    }
}