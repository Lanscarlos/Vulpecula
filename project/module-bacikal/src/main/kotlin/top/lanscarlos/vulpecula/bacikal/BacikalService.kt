package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.*
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalService {

    val compileNamespace: List<String>

    val questCompiler: BacikalQuestCompiler

    val questExecutor: BacikalQuestExecutor

    fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest

    fun buildSimpleQuest(name: String, func: Consumer<BacikalBlockBuilder>): BacikalQuest

}