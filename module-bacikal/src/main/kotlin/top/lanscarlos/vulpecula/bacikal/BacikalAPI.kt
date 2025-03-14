package top.lanscarlos.vulpecula.bacikal

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestCompiler
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestExecutor
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-11-20 16:35
 */
object BacikalAPI {

    /**
     * 编译 Kether 任务
     *
     * @param name 任务名称
     * @param source 任务源码
     * @param namespace 命名空间
     */
    fun compile(source: String, name: String, namespace: List<String>): Quest {
        return BacikalQuestCompiler.compile(name, source, namespace)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param sender 执行者
     * @param args 参数
     */
    fun execute(quest: Quest, name: String, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<*> {
        return BacikalQuestExecutor.execute(quest, name, sender, args)
    }

}