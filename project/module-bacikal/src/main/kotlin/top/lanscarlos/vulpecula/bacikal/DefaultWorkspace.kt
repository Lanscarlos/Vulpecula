package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-09-03 13:07
 */
class DefaultWorkspace(override val directory: File) : BacikalWorkspace {

    override val quests = mutableMapOf<String, BacikalScript>()

    override val runningQuests = mutableMapOf<String, MutableList<BacikalQuestContext>>()

    override fun loadQuest(file: File) {
        if (file.name[0] == '#' || file.isDirectory || file.extension != "ks") {
            return
        }
        val name = file.relativeTo(directory).path.substringBeforeLast('.').replace(File.separatorChar, '.')
        quests[name] = DefaultScript(name, file)
    }

    override fun loadQuests() {
        val queue = ArrayDeque<File>()
        queue.add(directory)
        while (queue.isNotEmpty()) {
            val file = queue.removeFirst()
            if (file.name[0] == '#') {
                continue
            }
            if (file.isDirectory) {
                queue.addAll(file.listFiles() ?: emptyArray())
                continue
            }
            loadQuest(file)
        }
    }

    override fun getRunningQuests(id: String): List<BacikalQuestContext> {
        return runningQuests[id] ?: emptyList()
    }

    override fun runActions(quest: BacikalQuest, func: BacikalQuestContext.() -> Unit): CompletableFuture<Any?> {
        val id = quest.name
        val context = Bacikal.service.createQuestContext(quest).also(func)
        runningQuests.computeIfAbsent(id) { mutableListOf() } += context

        return context.runActions().thenApply { value ->
            runningQuests[id]?.removeAll { it == context }
            value
        }
    }

    override fun terminateQuest(id: String) {
        val queue = runningQuests[id] ?: return
        queue.forEach { it.terminate() }
    }
}