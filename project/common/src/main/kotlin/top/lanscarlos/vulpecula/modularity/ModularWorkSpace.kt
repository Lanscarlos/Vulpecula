package top.lanscarlos.vulpecula.modularity

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestContext
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-09-02 10:33
 */
interface ModularWorkSpace {

    /**
     * 所属模块
     * */
    val module: Module

    val quests: Map<String, BacikalQuest>

    val runningQuests: Map<String, CompletableFuture<*>>

    /**
     * 运行任务
     * */
    fun runQuest(id: String, context: BacikalQuestContext.() -> Unit): CompletableFuture<*>

    /**
     * 运行任务
     * */
    fun runQuest(id: String, context: Consumer<BacikalQuestContext>): CompletableFuture<*>

    /**
     * 终止任务
     * */
    fun terminateQuest(id: String)

}