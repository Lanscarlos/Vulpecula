package top.lanscarlos.vulpecula.module.script

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.onlinePlayers
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.common.applicative.LocationApplicative
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.pow

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-05-18 10:39
 */
class ScriptExecutor {

    private lateinit var script: Script

    private var sender: List<ProxyCommandSender> = emptyList()

    private var args: List<Any> = emptyList()

    private var variables: Map<String, Any> = emptyMap()

    private var onSuccess: Consumer<Any?> = Consumer {  }

    private var onFailure: Function<BacikalRuntimeException, Any?> = Function { it }

    fun script(id: String): ScriptExecutor {
        return script(ScriptService.get(id))
    }

    fun script(script: Script): ScriptExecutor {
        this.script = script
        return this
    }

    fun sender(player: Player): ScriptExecutor {
        this.sender = listOf(adaptPlayer(player))
        return this
    }

    fun sender(sender: ProxyCommandSender): ScriptExecutor {
        this.sender = listOf(sender)
        return this
    }

    fun senderBy(selector: String): ScriptExecutor {
        if (selector.first() != '@') {
            // 指定玩家
            this.sender = Bukkit.getPlayerExact(selector)?.let(::adaptPlayer)?.let(::listOf)
                ?: error("无法选取脚本执行者 $selector")
            return this
        }
        val parser = selector.substring(1).split(' ')
        when (parser.first().lowercase()) {
            "null" -> {
                this.sender = emptyList()
            }
            "console" -> {
                this.sender = listOf(console())
            }
            "players" -> {
                this.sender = onlinePlayers()
            }
            "world" -> {
                this.sender = parser.getOrNull(1)?.let(Bukkit::getWorld)?.players?.map(::adaptPlayer)
                    ?: error("无法解析世界 ${parser.getOrNull(1)}")
            }
            "range" -> {
                val location = parser.getOrNull(1)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${parser.getOrNull(1)}")
                val world = location.world
                    ?: error("坐标不合法 ${parser.getOrNull(1)}")
                val range = parser.getOrNull(2)?.toDoubleOrNull()?.pow(2)
                    ?: error("无法解析范围 ${parser.getOrNull(2)}")
                info("@Range 距离平方 >> $range")
                this.sender = world.players.filter { it.location.distanceSquared(location) <= range }.map(::adaptPlayer)
            }
            "area" -> {
                val loc1 = parser.getOrNull(1)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${parser.getOrNull(1)}")
                val loc2 = parser.getOrNull(2)?.let(LocationApplicative::convertOrNull)?.toBukkitLocation()
                    ?: error("无法解析坐标 ${parser.getOrNull(2)}")
                val boundingBox = BoundingBox.of(loc1, loc2)
                val world = loc1.world!!
                this.sender = world.players.filter { boundingBox.contains(it.location.x, it.location.y, it.location.z) }.map(::adaptPlayer)
            }
            else -> error("Unknown sender: $selector")
        }
        return this
    }

    fun args(args: List<Any>): ScriptExecutor {
        this.args = args
        return this
    }

    fun variables(variables: Map<String, Any>): ScriptExecutor {
        this.variables = variables
        return this
    }

    fun onSuccess(func: Consumer<Any?>): ScriptExecutor {
        this.onSuccess = func
        return this
    }

    fun onFailure(func: Function<BacikalRuntimeException, Any?>): ScriptExecutor {
        this.onFailure = func
        return this
    }

    fun execute(): ScriptTask {
        if (sender.isEmpty()) {
            return script.execute(console(), args, variables, onSuccess, onFailure)
        }
        if (sender.size == 1) {
            return script.execute(sender.first(), args, variables, onSuccess, onFailure)
        }
        val tasks = sender.map { script.execute(sender.first(), args, variables, onSuccess, onFailure) }
        return ComplexScriptTask(ScriptService.nextPid(), script, tasks)
    }

}