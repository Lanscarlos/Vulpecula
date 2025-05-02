package top.lanscarlos.vulpecula.module.script

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.bacikal.BacikalService
import top.lanscarlos.vulpecula.bacikal.quest.BacikalRuntimeException
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.function.Function

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:12
 */
class NativeScript(override val id: String, source: String) : Script {

    constructor(source: String) : this(source.digest("MD5"), source)

    constructor(id: String, file: File) : this(id, file.readText(StandardCharsets.UTF_8))

    private val quest: Quest = BacikalService.compile(source, id, listOf("vulpecula"))

    override fun execute(
        sender: ProxyCommandSender?,
        args: List<Any?>,
        onSucceeded: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val wrappedArgs = mutableMapOf<String, Any>()
        wrappedArgs["args"] = args
        for ((index, arg) in args.withIndex()) {
            wrappedArgs["arg$index"] = arg ?: continue
        }
        return execute(sender, wrappedArgs, onSucceeded, onFailure)
    }

    override fun execute(
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSucceeded: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val pid = ScriptService.nextPid()
        val startTime = System.currentTimeMillis()
        val context = BacikalService.executeLater(quest, sender, args)
        val future = context.runActions().handle { result, e ->
            ScriptService.clearTask(pid)
            if (e == null) {
                onSucceeded.accept(result)
                return@handle result
            }
            val ex = e.cause as BacikalRuntimeException
            ex.printKetherErrorMessage()
            return@handle onFailure.apply(ex)
        }
        return DefaultScriptTask(pid, this, context, future, startTime).also(ScriptService::trackTask)
    }

}