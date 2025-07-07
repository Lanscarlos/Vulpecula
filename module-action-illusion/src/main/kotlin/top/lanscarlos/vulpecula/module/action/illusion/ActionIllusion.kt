package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.command.suggestPlayers
import taboolib.module.chat.colored
import top.lanscarlos.vulpecula.module.volatility.VolatileEntityMetadata

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
object ActionIllusion {

    @CommandBody
    val illusion = subCommand {
        literal("glow", literal = glow)
        literal("pose", literal = pose)
    }

    private val glow: CommandComponent.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val entity = selectEntity(sender)
            if (entity == null) {
                sender.sendMessage("&c请瞄准一个实体!".colored())
                return@execute
            }
            sender.sendMessage("&a成功瞄准 ${entity.type.name}!".colored())
            VolatileEntityMetadata.setGlowing(sender, entity, !sender.isGlowing)
        }


        dynamic {
            suggestPlayers()
            execute<Player> { sender, _, name ->
                val player = Bukkit.getPlayerExact(name)!!
                VolatileEntityMetadata.setGlowing(sender, player, !player.isGlowing)
            }
        }
    }

    private val pose: CommandComponent.() -> Unit = {
        dynamic {
            suggest { Pose.entries.map { it.name }.toList() }
            execute<Player> { sender, _, type ->
                val entity = selectEntity(sender)
                if (entity == null) {
                    sender.sendMessage("&c请瞄准一个实体!".colored())
                    return@execute
                }
                sender.sendMessage("&a成功瞄准 ${entity.type.name}!".colored())
                VolatileEntityMetadata.setPose(sender, entity, Pose.valueOf(type.uppercase()))
            }
        }.dynamic {
            suggestPlayers()
            execute<Player> { sender, content, name ->
                val type = content["type"]
                val player = Bukkit.getPlayerExact(name)!!
                VolatileEntityMetadata.setPose(sender, player, Pose.valueOf(type.uppercase()))
            }
        }
    }

    private fun selectEntity(viewer: Player): LivingEntity? {
        val result = viewer.world.rayTraceEntities(viewer.eyeLocation, viewer.eyeLocation.direction, 10.0) {
            it != viewer && it is LivingEntity
        }
        return result?.hitEntity as? LivingEntity
    }

}