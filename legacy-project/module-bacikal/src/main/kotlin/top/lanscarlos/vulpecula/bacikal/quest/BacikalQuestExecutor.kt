package top.lanscarlos.vulpecula.bacikal.quest

import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-01-05 17:31
 */
interface BacikalQuestExecutor {

    /**
     * 创建脚本上下文环境
     * @param quest 脚本
     * @param entry 入口主函数名
     * */
    fun createContext(quest: BacikalQuest, entry: String = "main"): BacikalQuestContext

    /**
     * 执行脚本
     * @param quest 脚本
     * @param entry 入口主函数名
     * */
    fun execute(quest: BacikalQuest, entry: String = "main", func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?>

}