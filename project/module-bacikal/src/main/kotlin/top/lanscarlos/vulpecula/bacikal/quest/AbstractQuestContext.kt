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
abstract class AbstractQuestContext(override val quest: BacikalQuest) : BacikalQuestContext {

    override var sender: ProxyCommandSender?
        get() = source.sender
        set(value) { source.sender = value }

    val source: ScriptContext by lazy { InnerContext() }


    override fun initVariables(variables: Map<String, Any>) {
        variables.forEach { (key, value) -> source[key] = value }
    }

    override fun <T> getVariable(key: String): T? {
        return source.get<T>(key)
    }

    override fun setVariable(key: String, value: Any?) {
        source[key] = value
    }

    override fun setVariables(vararg key: String, value: Any?) {
        key.forEach { source[it] = value }
    }

    override fun runActions(): CompletableFuture<Any?> {
        return source.runActions().exceptionally { ex ->
            ex.printKetherErrorMessage(true)
            warning("Quest ${quest.name} run failed: ${ex.localizedMessage}")
        }.thenApply {
            it
        }
    }

    override fun terminate() {
        source.terminate()
    }

    abstract fun createRootFrame(context: InnerContext): QuestContext.Frame

    inner class InnerContext : ScriptContext(ScriptService, quest.source) {
        override fun createRootFrame(): QuestContext.Frame {
            return this@AbstractQuestContext.createRootFrame(this)
        }

        fun superCreateRootFrame(): QuestContext.Frame {
            return super.createRootFrame()
        }
    }
}