package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestContext
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-09-03 13:02
 */
interface BacikalWorkspace {

    /**
     * 空间目录
     * */
    val directory: File

    /**
     * 任务列表
     * */
    val quests: Map<String, BacikalQuest>

    /**
     * 运行中的任务
     * */
    val runningQuests: Map<String, List<BacikalQuestContext>>

    /**
     * 加载任务
     * */
    fun loadQuest(file: File)

    /**
     * 加载所有任务
     * */
    fun loadQuests()

    /**
     * 获取运行中的任务
     * */
    fun getRunningQuests(id: String): List<BacikalQuestContext>

    /**
     * 运行任务
     * */
    fun runActions(quest: BacikalQuest, func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?>

    /**
     * 终止任务
     * */
    fun terminateQuest(id: String)

}