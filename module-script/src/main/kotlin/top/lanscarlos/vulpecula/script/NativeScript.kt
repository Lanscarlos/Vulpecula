package top.lanscarlos.vulpecula.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import top.lanscarlos.vulpecula.bacikal.BacikalAPI
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:12
 */
class NativeScript(val id: String, val file: File) : Script {

    private val quest: Quest

    init {
        quest = BacikalAPI.compile(file.readText(StandardCharsets.UTF_8), id, listOf("vulpecula"))
    }

    override fun runActions(sender: ProxyCommandSender?, args: Map<String, Any>): CompletableFuture<*> {
        return BacikalAPI.execute(quest, sender, args)
    }

}