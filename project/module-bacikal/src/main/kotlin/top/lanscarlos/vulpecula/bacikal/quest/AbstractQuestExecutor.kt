package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.warning
import taboolib.library.kether.QuestContext
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptService
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 11:24
 */
abstract class AbstractQuestExecutor(override val quest: BacikalQuest, override val entry: String) : BacikalQuestContext {

    val context: ScriptContext by lazy { InnerContext() }

    override var sender: ProxyCommandSender?
        get() = context.sender
        set(value) { context.sender = value }


    override fun initVariables(variables: Map<String, Any>) {
        variables.forEach { (key, value) -> context[key] = value }
    }

    override fun <T> getVariable(key: String): T? {
        return context.get<T>(key)
    }

    override fun setVariable(key: String, value: Any?) {
        context[key] = value
    }

    override fun setVariables(vararg key: String, value: Any?) {
        key.forEach { context[it] = value }
    }

    override fun runActions(): CompletableFuture<Any?> {
        return context.runActions().exceptionally { ex ->
            ex.printKetherErrorMessage(true)
            warning("Quest ${quest.name} run failed: ${ex.localizedMessage}")
        }
    }

    override fun terminate() {
        context.terminate()
    }

    abstract fun createRootFrame(context: InnerContext): QuestContext.Frame

    inner class InnerContext : ScriptContext(ScriptService, quest.source) {
        override fun createRootFrame(): QuestContext.Frame {
            return this@AbstractQuestExecutor.createRootFrame(this)
        }
    }
}