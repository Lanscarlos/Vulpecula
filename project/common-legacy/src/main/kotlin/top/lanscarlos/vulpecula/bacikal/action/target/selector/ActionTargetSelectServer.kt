package top.lanscarlos.vulpecula.bacikal.action.target.selector

import org.bukkit.Bukkit
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.selector
 *
 * @author Lanscarlos
 * @since 2023-03-23 09:08
 */
object ActionTargetSelectServer : ActionTarget.Resolver {

    override val name: Array<String> = arrayOf("PlayerOnServer", "PlayersOnServer", "Server")

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        return reader.transfer {
            combine(
                source()
            ) { target ->
                target.add(Bukkit.getOnlinePlayers())
                target
            }
        }
    }
}