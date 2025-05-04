package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.library.kether.QuestContext
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptService
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:37
 */
object KetherQuestExecutor {

    fun execute(quest: Quest, main: String, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<Any?> {
        return executeLater(quest, main, sender, args).runActions()
    }

    fun execute(quest: Quest, main: String, func: (ScriptContext) -> Unit): CompletableFuture<Any?> {
        return executeLater(quest, main, func).runActions()
    }

    fun executeLater(quest: Quest, main: String, sender: ProxyCommandSender?, args: Map<String, Any?>): ScriptContext {
        return executeLater(quest, main) {
            it.sender = sender
            for (entry in args) {
                it[entry.key] = entry.value
            }
        }
    }

    fun executeLater(quest: Quest, main: String, func: (ScriptContext) -> Unit): ScriptContext {
        val context = object : ScriptContext(ScriptService, quest) {
            override fun createRootFrame(): QuestContext.Frame {
                return SimpleNamedFrame(null, mutableListOf(), SimpleVarTable(null), main, this)
            }
        }
        return context.also(func)
    }
}