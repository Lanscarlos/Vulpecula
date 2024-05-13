package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestReader
import java.io.File
import java.net.URLDecoder
import java.util.*
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-05-13 17:02
 */
abstract class BacikalComplexActionParser : QuestActionParser {

    abstract val name: String

    abstract val author: Array<String>

    abstract val mapped: Map<String, BacikalActionParser>

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        reader.mark()
        val next = reader.nextToken()
        val parser = mapped[next] ?: error("Unknown action '$next' at $name")
        return parser.resolve(reader)
    }

    fun setup() {
        val packetName = this::class.java.`package`.name
        val classLoader = this::class.java.classLoader
        val resources = classLoader.getResources(packetName.replace(".", "/"))

        while (resources.hasMoreElements()) {
            val url = resources.nextElement()
            if (url?.protocol != "file") {
                continue
            }

            val folder = File(URLDecoder.decode(url.path,"utf-8"))
            for (file in folder.deepFiles()) {
                if (!file.name.endsWith(".class")) {
                    continue
                }
                val className = packetName + "." + file.nameWithoutExtension
                if (className == this::class.java.name) {
                    continue
                }
                try {
                    val clazz = Class.forName(className)
                    if (clazz.`package`.name == packetName) {
                        // todo 注意单例 不能在这里实例化
                        visitClass(clazz) { clazz.getDeclaredConstructor().newInstance() }
                    }
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    abstract fun visitClass(clazz: Class<*>, supplier: Supplier<*>?)

    private fun File.deepFiles(): List<File> {
        if (!this.isDirectory) {
            return listOf(this)
        }
        val files = mutableListOf<File>()
        val stack = Stack<File>()
        stack.push(this)

        while (stack.isNotEmpty()) {
            val directory = stack.pop()
            for (file in directory?.listFiles() ?: continue) {
                if (file.isDirectory) {
                    stack.push(file)
                } else {
                    files.add(file)
                }
            }
        }
        return files
    }

}