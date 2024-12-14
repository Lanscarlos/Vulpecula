package top.lanscarlos.vulpecula.world

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.world.ChunkLoadEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.command
import taboolib.common.platform.command.restrictDouble
import taboolib.common.platform.command.suggest
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common5.cdouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.world
 *
 * @author Lanscarlos
 * @since 2024-12-12 11:28
 */
object VulpeculaPlugin {

    @SubscribeEvent
    fun e(e: ChunkLoadEvent) {
        if (e.isNewChunk) {
            info("ChunkLoadEvent >> creating new Chunk. ChunkX: ${e.chunk.x}, ChunkZ: ${e.chunk.z}")
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {

        // 尝试加载世界?
        val creator = WorldCreator("world_vul")
//        creator.environment(World.Environment.NORMAL)
        creator.generator(CustomGenerator())
        Bukkit.createWorld(creator)

        command("test") {
            literal("info") {
                execute<Player> { sender, _, _ ->
                    sender.sendMessage("World: ${sender.world.name}; X: ${sender.location.x}; Y: ${sender.location.y}; Z: ${sender.location.z}")
                }
            }
            literal("tp") {
                dynamic("world") {
                    suggest { Bukkit.getWorlds().map { it.name } }
                    dynamic("x") {
                        restrictDouble()
                        dynamic("y") {
                            restrictDouble()
                            dynamic("z") {
                                restrictDouble()
                                execute<Player> { sender, context, value ->
                                    val world = Bukkit.getWorld(context["world"])!!
                                    val x = context["x"].cdouble
                                    val y = context["y"].cdouble
                                    val z = value.cdouble
                                    val location = Location(world, x, y, z)
                                    sender.teleport(location)
                                }
                            }
                        }
                    }
                }
            }
            literal("create") {
                dynamic("world") {
                    execute<Player> { sender, _, value ->
                        val creator = WorldCreator(value)
                        creator.environment(World.Environment.NORMAL)
                        creator.generator(CustomGenerator())
                        Bukkit.createWorld(creator)
                        sender.sendMessage("世界 $value 创建成功")
                    }
                }
            }
        }
    }

}