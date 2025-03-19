package top.lanscarlos.vulpecula.bacikal.quest

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.kether.Quest
import taboolib.module.kether.ScriptContext
import java.util.LinkedList
import java.util.concurrent.CompletableFuture
import java.util.function.Function

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

    val quest: Quest
        get() = source.quest

    var onException: LinkedList<Function<Throwable, Any?>> = LinkedList()

    override fun get(key: String): Any? {
        return getVariable(key)
    }

    override fun set(key: String, value: Any?) {
        return setVariable(key, value)
    }

    override fun <T> getVariable(key: String): T? {
        return getVariable(key, null)
    }

    override fun <T> getVariable(key: String, default: T): T {
        return source.rootFrame().variables().get<T>(key).orElse(default)
    }

    override fun <T> getVariables(vararg key: String): T? {
        return key.firstNotNullOfOrNull { getVariable<T>(it) }
    }

    override fun setVariable(key: String, value: Any?) {
        source.rootFrame().variables().set(key, value)
    }

    override fun setVariables(vararg key: String, value: Any?) {
        key.forEach { setVariable(it, value) }
    }

    override fun variables(): MutableMap<String, Any> {
        return source.rootFrame().variables().toMap()
    }

    override fun runActions(): CompletableFuture<*> {
        var future = source.runActions()

        if (quest.getBlock("@EXCEPTIONALLY").isPresent) {
            // 异常处理
            future = future.exceptionallyCompose { ex ->
                BacikalQuestExecutor.QuestExecutorContext(quest, "@EXCEPTIONALLY").also {
                    // 传入原始参数
                    for ((key, value) in variables()) {
                        it[key] = value
                    }
                    it["exception"] = ex
                    it["ex"] = ex
                    it["exception-message"] = ex.message
                    it["ex-message"] = ex.message
                }.runActions()
            }
        }

        // 自定义异常处理
        if (onException.isNotEmpty()) {
            for (func in onException) {
                future = future.exceptionally(func)
            }
        }

        return future
    }

    override fun terminate() {
        source.terminate()
    }

    override fun exceptionally(func: Function<Throwable, Any?>) {
        onException += func
    }

}