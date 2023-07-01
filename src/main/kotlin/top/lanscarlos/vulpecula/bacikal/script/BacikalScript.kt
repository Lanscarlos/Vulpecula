package top.lanscarlos.vulpecula.bacikal.script

import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.module.kether.Script
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.parseKetherScript
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.script
 *
 * @author Lanscarlos
 * @since 2023-05-09 11:15
 */
class BacikalScript(val source: String, namespace: List<String> = emptyList(), compile: Boolean = true) {

    val namespace = namespace.toMutableList()
    var script: Script? = null

    // 阻断器 5s
    private var baffle = -1L
    // 异常缓存
    private var throwable: Throwable? = null

    init {
        if (compile) {
            compile()
        }
    }

    /**
     * 编译脚本
     * @return 编译成功返回 true 反之 false
     * */
    fun compile(namespace: List<String> = this.namespace): Boolean {
        return try {
            if (source.isEmpty()) {
                error("Script source must not empty.")
            }

            script = if (namespace.contains("vulpecula")) {
                source.parseKetherScript(namespace)
            } else {
                source.parseKetherScript(namespace.plus("vulpecula"))
            }
            true
        } catch (e: Exception) {
            throwable = e
            e.printKetherErrorMessage()
            false
        }
    }

    /**
     * 执行脚本
     * */
    fun runActions(
        sender: Any? = null,
        variables: Map<String, Any?> = emptyMap()
    ): CompletableFuture<Any?> {
        return runActions {
            // 传入执行者
            this.sender = if (sender != null) {
                adaptCommandSender(sender)
            } else {
                console()
            }

            // 传入变量
            for (it in variables) {
                set(it.key, it.value)
            }
        }.exceptionally { e ->
            e.printKetherErrorMessage()
            null
        }
    }

    /**
     * 执行脚本
     * */
    fun runActions(func: ScriptContext.() -> Unit): CompletableFuture<Any?> {
        if (script == null) {
            if (System.currentTimeMillis() > baffle) {
                throwable?.printKetherErrorMessage(detailError = true) ?: error("Script has not compile yet!")
                baffle = System.currentTimeMillis() + 5000L
            }
        }
        return try {
            ScriptContext.create(script!!).also(func).runActions()
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(null)
        }
    }
}