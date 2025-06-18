package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.module.kether.ScriptContext
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestCompiler
import top.lanscarlos.vulpecula.module.bacikal.quest.BacikalQuestExecutor
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * @author Lanscarlos
 * @since 2025/4/25 17:02
 */
object BacikalService {

    internal val module: String get() = asLang("module-bacikal-service-name")

    /**
     * 编译 Kether 任务
     *
     * @param file 文件
     * @param namespace 命名空间
     */
    fun compile(file: File, namespace: List<String>): Quest {
        return BacikalQuestCompiler.compile(file, namespace)
    }

    /**
     * 编译 Kether 任务
     *
     * @param source 任务源码
     * @param name 任务名称
     * @param namespace 命名空间
     * @throws BacikalCompileException 编译错误
     */
    fun compile(source: String, name: String, namespace: List<String>): Quest {
        return BacikalQuestCompiler.compile(source, name, namespace)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param sender 执行者
     * @param args 参数
     */
    fun execute(quest: Quest, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<Any?> {
        return execute(quest, "main", timeout, sender, args)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param main 入口函数
     * @param sender 执行者
     * @param args 参数
     */
    fun execute(quest: Quest, main: String, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): CompletableFuture<Any?> {
        return BacikalQuestExecutor.execute(quest, main, timeout, sender, args)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     */
    fun execute(quest: Quest, timeout: Long, func: (ScriptContext) -> Unit): CompletableFuture<Any?> {
        return execute(quest, "main", timeout, func)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param main 入口函数
     */
    fun execute(quest: Quest, main: String, timeout: Long, func: (ScriptContext) -> Unit): CompletableFuture<Any?> {
        return BacikalQuestExecutor.execute(quest, main, timeout, func)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param sender 执行者
     * @param args 参数
     */
    fun executeLater(quest: Quest, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): ScriptContext {
        return executeLater(quest, "main", timeout, sender, args)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param main 入口函数
     * @param sender 执行者
     * @param args 参数
     */
    fun executeLater(quest: Quest, main: String, timeout: Long, sender: ProxyCommandSender?, args: Map<String, Any?>): ScriptContext {
        return BacikalQuestExecutor.executeLater(quest, main, timeout, sender, args)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     */
    fun executeLater(quest: Quest, timeout: Long, func: (ScriptContext) -> Unit): ScriptContext {
        return executeLater(quest, "main", timeout, func)
    }

    /**
     * 执行 Kether 任务
     *
     * @param quest 任务
     * @param main 入口函数
     */
    fun executeLater(quest: Quest, main: String, timeout: Long, func: (ScriptContext) -> Unit): ScriptContext {
        return BacikalQuestExecutor.executeLater(quest, main, timeout, func)
    }

}