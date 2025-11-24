package top.lanscarlos.vulpecula.module.script

import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.module.bacikal.BacikalService
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:12
 */
class NativeScript(override val id: String, source: String) : AbstractScript() {

    constructor(source: String) : this(source.digest("MD5"), source)

    constructor(id: String, file: File) : this(id, file.readText(StandardCharsets.UTF_8))

    override val quest: Quest = BacikalService.compile(source, id, listOf("vulpecula"))

    init {
        require(source.isNotBlank()) {
            "Source is not blank"
        }
    }

    override fun execute(
        sender: ProxyCommandSender?,
        args: List<Any?>,
        variables: Map<String, Any>
    ): ScriptTask {
        val wrappedArgs = mutableMapOf<String, Any>()
        wrappedArgs["args"] = args
        for ((index, arg) in args.withIndex()) {
            wrappedArgs["arg$index"] = arg ?: continue
        }
        wrappedArgs.putAll(variables)
        return execute(sender, wrappedArgs)
    }

    private fun execute(
        sender: ProxyCommandSender?,
        args: Map<String, Any>
    ): ScriptTask {
        val pid = ScriptService.nextPid()
        val startTime = System.currentTimeMillis()
        val context = BacikalService.executeLater(quest, -1L, sender, args)
        val future = context.runActions()
        return DefaultScriptTask(pid, this, context, future, startTime).also(ScriptService::trackTask)
    }

}