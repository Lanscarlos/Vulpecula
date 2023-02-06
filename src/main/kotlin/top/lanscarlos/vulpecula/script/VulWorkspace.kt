package top.lanscarlos.vulpecula.script

import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import org.bukkit.entity.Player
import taboolib.common.platform.function.*
import taboolib.library.kether.ExitStatus
import taboolib.module.kether.*
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2022-12-22 18:40
 */
@Suppress("UnstableApiUsage")
object VulWorkspace {

    val namespace = listOf("vulpecula", "vulpecula-script")
    val folder = File(getDataFolder(), "scripts/.compiled")
    val scriptLoader = KetherScriptLoader()
    val scripts = HashMap<String, Script>()
    val runningScripts = MultimapBuilder.hashKeys().arrayListValues().build<String, ScriptContext>()

    fun getRunningScript(): List<ScriptContext> {
        return ImmutableList.copyOf(runningScripts.values())
    }

    fun runScript(id: String, sender: Any? = null, args: Array<Any?> = emptyArray()): CompletableFuture<Any>? {
        val script = scripts[id] ?: return null
        return runScript(id, ScriptContext.create(script) {
            if (sender != null) {
                this.sender = adaptCommandSender(sender)
                if (sender is Player) {
                    rootFrame().variables().set("player", sender)
                }
            }
            for ((i, arg) in args.withIndex()) {
                rootFrame().variables().set("arg$i", arg)
            }
            rootFrame().variables().set("args", args)
        })
    }

    fun runScript(id: String, context: ScriptContext): CompletableFuture<Any> {
        context.id = id
        runningScripts.put(id, context)
        return try {
            context.runActions().also { it.thenRun { runningScripts.remove(id, context) } }
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(null)
        }
    }

    fun terminateScript(id: String) {
        runningScripts.removeAll(id).forEach { it.terminate() }
    }

    fun terminateScript(context: ScriptContext) {
        if (!context.exitStatus.isPresent) {
            context.setExitStatus(ExitStatus.paused())
            runningScripts.remove(context.id, context)
        }
    }

    fun terminateAllScript() {
        getRunningScript().forEach { terminateScript(it) }
    }

    fun loadScript(file: File, name: String) {
        // 读取文件
        val source = file.readText(StandardCharsets.UTF_8)
        scripts[name] = scriptLoader.load(ScriptService, name, source.toByteArray(StandardCharsets.UTF_8), namespace)
    }

    fun onFileChanged(file: File) {
        val start = timing()
        try {
            val name = folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '.').substringBeforeLast('.')
            loadScript(file, name)

            console().sendLang("Script-Load-Automatic-Succeeded", name, timing(start))
        } catch (e: Exception) {
            warning("Unexpected exception while parsing kether script:")
            e.localizedMessage?.split('\n')?.forEach { warning(it) }
        }
    }

    fun load(): String {
        val start = timing()
        return try {
            scripts.clear()

            val folder = folder.toPath()

            if (java.nio.file.Files.notExists(folder)) {
                // 路径不存在
                java.nio.file.Files.createDirectories(folder)
            }

            // 遍历文件
            val iterator = java.nio.file.Files.walk(folder).iterator()
            while (iterator.hasNext()) {
                val path = iterator.next()

                // 排除
                if (java.nio.file.Files.isDirectory(path)) continue
                if (!path.toString().endsWith(".ks")) continue
                if (path.fileName.toString().startsWith("#")) continue

                val name = folder.relativize(path).toString().replace(File.separatorChar, '.').substringBeforeLast('.')
                val file = path.toFile().addWatcher(false) { onFileChanged(this) }
                // 读取文件
                try {
                    loadScript(file, name)
                } catch (e: Exception) {
                    warning("Unexpected exception while parsing kether script:")
                    e.localizedMessage?.split('\n')?.forEach { warning(it) }
                }
            }

            console().asLangText("Script-Load-Succeeded", scripts.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            console().asLangText("Script-Load-Failed", e.localizedMessage, timing(start)).also {
                console().sendMessage(it)
            }
        }
    }
}