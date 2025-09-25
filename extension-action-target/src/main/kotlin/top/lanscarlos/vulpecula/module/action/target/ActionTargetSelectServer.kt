package top.lanscarlos.vulpecula.module.action.target

import org.bukkit.Bukkit
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/3
 */
@Parser("target.select.server")
object ActionTargetSelectServer : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame
    ) {
        val players = Bukkit.getOnlinePlayers()
        ActionTarget.setContext(frame, players)
    }

}