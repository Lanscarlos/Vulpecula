package top.lanscarlos.vulpecula.bacikal

import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.applicative.applicativeStringList
import top.lanscarlos.vulpecula.bacikal.quest.*
import top.lanscarlos.vulpecula.config.bindConfigSection
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:01
 */
object DefaultBacikalService : BacikalService {

    override val defaultCompileNamespace: List<String> by bindConfigSection("bacikal.default-compile-namespace") { value ->
        value?.applicativeStringList() ?: emptyList()
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

    override val questExecutor: BacikalQuestExecutor by bindConfigSection("bacikal.executor") { value ->
        when (value) {
            "kether" -> KetherQuestExecutor
            "coroutines" -> CoroutinesQuestExecutor
            else -> {
                warning("Unknown executor: $value, use kether executor.")
                KetherQuestExecutor
            }
        }
    }

    override fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest {
        val builder = DefaultQuestBuilder(name)
        func.accept(builder)
        return builder.build()
    }

    override fun executeQuest(quest: BacikalQuest): CompletableFuture<*> {
        TODO("Not yet implemented")
    }

    override fun terminateQuest(quest: BacikalQuest) {
        TODO("Not yet implemented")
    }

}