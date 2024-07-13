package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalService {

    /**
     * 默认编译命名空间
     * */
    val defaultCompileNamespace: List<String>

    val questCompiler: BacikalQuestCompiler

    val questExecutor: BacikalQuestExecutor

    fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest

    fun executeQuest(quest: BacikalQuest): CompletableFuture<*>

    fun terminateQuest(quest: BacikalQuest)

}