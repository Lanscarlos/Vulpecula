package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.bacikal.BacikalService
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:12
 */
class NativeScript(override val id: String, override val file: File) : Script {

    private val quest: Quest = BacikalService.compile(file.readText(StandardCharsets.UTF_8), id, listOf("vulpecula"))

    override fun buildQuest() {
        TODO("Not yet implemented")
    }

    override fun execute(sender: ProxyCommandSender?, args: List<Any?>): ScriptTask {
        val wrappedArgs = mutableMapOf<String, Any>()
        wrappedArgs["args"] = args
        for ((index, arg) in args.withIndex()) {
            wrappedArgs["arg$index"] = arg ?: continue
        }
        return execute(sender, wrappedArgs)
    }

    override fun execute(sender: ProxyCommandSender?, args: Map<String, Any>): ScriptTask {
        val pid = ScriptService.nextPid()
        val startTime = System.currentTimeMillis()
        val context = BacikalService.executeLater(quest, sender, args)
        val future = context.runActions()
        return DefaultScriptTask(pid, this, context, future, startTime)
    }

}