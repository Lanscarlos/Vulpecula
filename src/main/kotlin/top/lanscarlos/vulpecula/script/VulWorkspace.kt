package top.lanscarlos.vulpecula.script

import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import org.bukkit.Bukkit
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.library.kether.ExitStatus
import taboolib.module.kether.KetherScriptLoader
import taboolib.module.kether.Script
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptService
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

//    val commentPattern by bindConfigNode("script-setting.comment-pattern") {
//        (it?.toString() ?: "").toPattern()
//    }

    val namespace = listOf("Vulpecula", "vulpecula-script")

    val folder = File(getDataFolder(), "scripts/.compiled")

    val scripts = HashMap<String, Script>()
    val runningScripts = MultimapBuilder.hashKeys().arrayListValues().build<String, ScriptContext>()!!

    fun getRunningScript(): List<ScriptContext> {
        return ImmutableList.copyOf(runningScripts.values())
    }

    fun runScript(id: String, sender: String? = null, args: Array<String> = emptyArray()): CompletableFuture<Any>? {
        val script = scripts[id] ?: return null
        return runScript(id, ScriptContext.create(script) {
            if (sender != null) {
                this.sender = Bukkit.getPlayerExact(sender)?.let { adaptCommandSender(it) }
            }
            for ((i, arg) in args.withIndex()) {
                rootFrame().variables().set("arg${i}", arg)
            }
            rootFrame().variables().set("args", args)
        })
    }

    fun runScript(id: String, context: ScriptContext): CompletableFuture<Any> {
        context.id = id
        runningScripts.put(id, context)
        return context.runActions().also { it.thenRun { runningScripts.remove(id, context) } }
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

    fun onFileChanged(file: File) {
        val start = timing()
        try {
            val name = folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '.').substringBeforeLast('.')
            val source = file.readText(StandardCharsets.UTF_8)
            scripts[name] = KetherScriptLoader().load(ScriptService, name, source.toByteArray(StandardCharsets.UTF_8), namespace)

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

            val loader = KetherScriptLoader()
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
                    val source = file.readText(StandardCharsets.UTF_8)
                    scripts[name] = loader.load(ScriptService, name, source.toByteArray(StandardCharsets.UTF_8), namespace)
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