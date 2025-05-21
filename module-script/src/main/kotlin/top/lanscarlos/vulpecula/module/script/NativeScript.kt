package top.lanscarlos.vulpecula.module.script

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalRuntimeException
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

    override fun run(
        sender: ProxyCommandSender?,
        selector: SenderSelector,
        args: List<Any?>,
        variables: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val wrappedArgs = mutableMapOf<String, Any>()
        wrappedArgs["args"] = args
        for ((index, arg) in args.withIndex()) {
            wrappedArgs["arg$index"] = arg ?: continue
        }
        wrappedArgs.putAll(variables)
        return run(sender, wrappedArgs, onSuccess, onFailure)
    }

    private fun run(
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): ScriptTask {
        val pid = ScriptService.nextPid()
        val startTime = System.currentTimeMillis()
        val context = BacikalService.executeLater(quest, -1L, sender, args)
        val future = context.runActions().handle { result, e ->
            ScriptService.clearTask(pid)
            if (e == null) {
                onSuccess.accept(result)
                return@handle result
            }
            val ex = e.cause as BacikalRuntimeException
            ex.printKetherMessage()
            return@handle onFailure.apply(ex)
        }
        return DefaultScriptTask(pid, this, context, future, startTime).also(ScriptService::trackTask)
    }

}