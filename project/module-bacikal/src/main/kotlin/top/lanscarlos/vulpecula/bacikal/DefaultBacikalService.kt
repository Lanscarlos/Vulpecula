package top.lanscarlos.vulpecula.bacikal

import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.applicative.StringListApplicative.Companion.applicativeStringList
import top.lanscarlos.vulpecula.bacikal.quest.*
import top.lanscarlos.vulpecula.config.bindConfigSection
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:01
 */
object DefaultBacikalService : BacikalService {

    override val compileNamespace: List<String> by bindConfigSection("bacikal.compile-namespace") { value ->
        value?.applicativeStringList()?.getValue() ?: emptyList()
    }

    override val questCompiler: BacikalQuestCompiler by bindConfigSection("bacikal.compiler") { value ->
        when (value) {
            "bacikal" -> FixedQuestCompiler
            "kether" -> KetherQuestCompiler
            else -> {
                warning("Unknown compiler: $value, use bacikal compiler.")
                FixedQuestCompiler
            }
        }
    }

    val questContext: String by bindConfigSection("bacikal.context") { value ->
        value?.toString()?.lowercase() ?: "kether"
    }

    override fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest {
        val builder = DefaultQuestBuilder(name)
        func.accept(builder)
        return builder.build()
    }

    override fun buildSimpleQuest(name: String, func: Consumer<BacikalBlockBuilder>): BacikalQuest {
        return DefaultQuestBuilder(name).also { it.appendBlock(name, func) }.build()
    }

    override fun createQuestContext(quest: BacikalQuest): BacikalQuestContext {
        return when (questContext) {
            "coroutines" -> CoroutinesQuestContext(quest)
            "kether" -> KetherQuestContext(quest)
            else -> {
                warning("Unknown context: $questContext, use kether context.")
                KetherQuestContext(quest)
            }
        }
    }

}