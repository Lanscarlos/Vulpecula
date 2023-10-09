package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestContext
import top.lanscarlos.vulpecula.bacikal.quest.KetherQuest
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-09-03 21:36
 */
class DefaultScript(override val name: String, override val file: File) : BacikalScript {

    override val content: String = file.readText(StandardCharsets.UTF_8)

    override val source: KetherQuest = Bacikal.service.questCompiler.compile(name, content).source

    override fun runActions(func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        return Bacikal.service.createQuestContext(this).apply(func).runActions()
    }
}