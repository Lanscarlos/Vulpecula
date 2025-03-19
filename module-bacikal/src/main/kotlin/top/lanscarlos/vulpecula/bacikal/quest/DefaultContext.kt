package top.lanscarlos.vulpecula.bacikal.quest

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.ScriptContext
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-03-19 09:10
 */
class DefaultContext(var source: ScriptContext) : BacikalContext {

    override var sender: ProxyCommandSender?
        get() = source.sender
        set(value) {
            source.sender = value
        }

    override var senderAsPlayer: Player?
        get() = (sender as? ProxyPlayer)?.castSafely()
        set(value) {
            sender = value?.let(::adaptPlayer)
        }

    override val breakFlag: Boolean
        get() = source.breakLoop

    override val exitFlag: Boolean
        get() = source.exitStatus == null

    override fun get(key: String): Any? {
        TODO("Not yet implemented")
    }

    override fun set(key: String, value: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun <T> getVariable(key: String): T? {
        TODO("Not yet implemented")
    }

    override fun <T> getVariable(key: String, default: T): T {
        TODO("Not yet implemented")
    }

    override fun <T> getVariables(vararg key: String): T? {
        TODO("Not yet implemented")
    }

    override fun setVariable(key: String, value: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun setVariables(vararg key: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun variables(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun runActions(): CompletableFuture<*> {
        TODO("Not yet implemented")
    }

    override fun terminate() {
        TODO("Not yet implemented")
    }

}