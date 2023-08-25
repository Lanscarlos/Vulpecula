package top.lanscarlos.vulpecula.bacikal

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

    override val questCompiler: BacikalQuestCompiler by bindConfigSection("bacikal.compiler") { value ->
        when (value) {
            "bacikal" -> FixedQuestCompiler
            "kether" -> KetherQuestCompiler
            else -> throw IllegalArgumentException("Unknown compiler: $value")
        }
    }

    override fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest {
        val builder = DefaultQuestBuilder(name)
        func.accept(builder)
        return builder.build()
    }

    override fun buildSimpleQuest(name: String, func: Consumer<BacikalBlockBuilder>): BacikalQuest {
        return DefaultQuestBuilder(name).also { it.appendBlock(name, func) }.build()
    }

    override fun buildQuestContext(quest: BacikalQuest): BacikalQuestContext {
        return CoroutinesQuestContext(quest)
    }

}