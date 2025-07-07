package top.lanscarlos.vulpecula.module.action.illusion

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggestPlayers
import taboolib.common.platform.function.info
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.chat.colored
import top.lanscarlos.vulpecula.module.volatility.VolatileEntityMetadata
import java.util.function.Predicate

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
        literal("swimming", literal = swimming)
        literal("sleeping", literal = sleeping)
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

    private val swimming: CommandComponent.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val entity = selectEntity(sender)
            if (entity == null) {
                sender.sendMessage("&c请瞄准一个实体!".colored())
                return@execute
            }
            sender.sendMessage("&a成功瞄准 ${entity.type.name}!".colored())
            VolatileEntityMetadata.setSwimming(sender, entity, !sender.isSwimming)
            sender.invokeMethod<Void>("setPose", Pose.SWIMMING, true)
        }

        dynamic {
            suggestPlayers()
            execute<Player> { sender, _, name ->
                val player = Bukkit.getPlayerExact(name)!!
                VolatileEntityMetadata.setSwimming(sender, player, !player.isSwimming)
            }
        }
    }

    private val sleeping: CommandComponent.() -> Unit = {
        execute<Player> { sender, _, _ ->
            val entity = selectEntity(sender)
            if (entity == null) {
                sender.sendMessage("&c请瞄准一个实体!".colored())
                return@execute
            }
            sender.sendMessage("&a成功瞄准 ${entity.type.name}!".colored())
            VolatileEntityMetadata.setSleep(sender, entity, !sender.isSleeping)
        }

        dynamic {
            suggestPlayers()
            execute<Player> { sender, _, name ->
                val player = Bukkit.getPlayerExact(name)!!
                VolatileEntityMetadata.setSleep(sender, player, !player.isSleeping)
            }
        }
    }

    fun selectEntity(viewer: Player): LivingEntity? {
        val result = viewer.world.rayTraceEntities(viewer.eyeLocation, viewer.eyeLocation.direction, 10.0) {
            it != viewer && it is LivingEntity
        }
        return result?.hitEntity as? LivingEntity
    }

}