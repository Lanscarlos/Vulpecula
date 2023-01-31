package top.lanscarlos.vulpecula.internal

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.function.*
import taboolib.expansion.createHelper
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.kether.parseKetherScript
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.config.VulConfig
import top.lanscarlos.vulpecula.script.ScriptCompiler
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import java.io.File
import java.nio.file.Files

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2022-12-24 23:44
 */
class CustomCommand(
    val id: String,
    val wrapper: VulConfig
) : ScriptCompiler {

    val name by wrapper.readString("name")
    val aliases by wrapper.readStringList("aliases")
    val description by wrapper.readString("description", "")
    val usage by wrapper.readString("usage", "")
    val permission by wrapper.readString("permission", "")
    val permissionMessage by wrapper.readString("permission-message", "")
    val permissionDefault by wrapper.read("permission-default") { value ->
        PermissionDefault.values().firstOrNull {
            it.name.equals(value?.toString(), true)
        } ?: PermissionDefault.OP
    }
    val component by wrapper.read("components") { section ->
        val root = CommandBase()
        if (section !is ConfigurationSection) return@read root

        // 处理执行语句
        section.getString("main.execute")?.let {
            root.executeScript(it, section.getBoolean("main.player-only", false), false)
        }

        // 遍历下一层 Dynamic
        section.getConfigurationSection("dynamic")?.let { next ->
            buildDynamic(root, next)
        }

        // 遍历下一层 Literal
        section.getConfigurationSection("literal")?.let { next ->
            buildLiteral(root, next)
        }

        // 创建语句帮手
        if (section.getBoolean("main.helper", true)) root.createHelper()

        return@read root
    }

    init {
        register()
    }

    fun register() {
        registerCommand(
            // 创建命令结构
            command = CommandStructure(
                name ?: error("Custom command name undefined."),
                aliases,
                description,
                usage,
                permission,
                permissionMessage,
                permissionDefault,
                permissionChildren = emptyMap()
            ),
            // 创建执行器
            executor = object : CommandExecutor {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                    return component.execute(CommandContext(sender, command, name, component, args))
                }
            },
            // 创建补全器
            completer = object : CommandCompleter {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                    return component.suggest(CommandContext(sender, command, name, component, args))
                }
            },
            // 传入原始命令构建器
            commandBuilder = {}
        )
    }

    fun unregister() {
        if (aliases.isNotEmpty()) aliases.forEach { unregisterCommand(it) }
        unregisterCommand(name ?: return)
    }

    fun buildDynamic(parent: CommandComponent, section: ConfigurationSection) {
        val dynamic = CommandComponentDynamic(
            parent.index + 1,
            section.getString("comment") ?: "...",
            section.getBoolean("optional", false),
            section.getString("permission") ?: ""
        )

        // 加入到父节点
        parent.children += dynamic

        val playerOnly = section.getBoolean("player-only", false)

        section["suggest"]?.let { suggest ->
            val uncheck = section.getBoolean("uncheck", false)
            when (suggest) {
                "*", "@Players", "@players" -> {
                    dynamic.suggestion<ProxyCommandSender>(uncheck) { _, _ -> onlinePlayers().map { it.name }.toMutableList() }
                }
                is Collection<*> -> dynamic.suggestLiteral(suggest, playerOnly, uncheck)
                else -> {
                    val force = section.getBoolean("suggest-force", false)
                    dynamic.suggestScript(suggest, playerOnly, uncheck, force)
                }
            }
        }

        section["execute"]?.let {
            dynamic.executeScript(it, playerOnly, true)
        }

        // 遍历下一层 Dynamic
        section.getConfigurationSection("dynamic")?.let { nextDynamic ->
            buildDynamic(dynamic, nextDynamic)
        }

        // 遍历下一层 Literal
        section.getConfigurationSection("literal")?.let { next ->
            buildLiteral(dynamic, next)
        }
    }

    fun buildLiteral(parent: CommandComponent, config: ConfigurationSection) {
        val keys = config.getKeys(false)
        for (key in keys) {
            val section = config.getConfigurationSection(key) ?: continue
            val name = section.getString("name") ?: key
            val aliases = section.getStringOrList("aliases")

            val literal = CommandComponentLiteral(
                parent.index + 1,
                *aliases.plus(name).toTypedArray(),
                optional = section.getBoolean("optional", false),
                permission = section.getString("permission") ?: ""
            )

            parent.children += literal

            section["execute"]?.let {
                literal.executeScript(it, section.getBoolean("player-only", false), true)
            }

            // 遍历下一层 Dynamic
            section.getConfigurationSection("dynamic")?.let { next ->
                buildDynamic(literal, next)
            }

            // 遍历下一层 Literal
            section.getConfigurationSection("literal")?.let { next ->
                buildLiteral(literal, next)
            }
        }
    }

    fun CommandComponentDynamic.suggestLiteral(literal: Collection<*>, playerOnly: Boolean, uncheck: Boolean) {
        if (playerOnly) {
            this.suggestion<Player>(uncheck) { _, _ -> literal.mapNotNull { it?.toString() } }
        } else {
            this.suggestion<CommandSender>(uncheck) { _, _ -> literal.mapNotNull { it?.toString() } }
        }
    }

    /**
     * @param force 是否强制等待脚本返回结果
     * */
    fun CommandComponentDynamic.suggestScript(source: Any, playerOnly: Boolean, uncheck: Boolean, force: Boolean) {
        val builder = StringBuilder()
        val content = buildSection(source, builder).extract()
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        val script = try {
            builder.toString().parseKetherScript()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        fun handle(sender: Any, context: CommandContext<*>): List<String> {
            val future = script.runActions {
                setVariable("@Sender", value = sender)
                if (sender is Player) setVariable("@Player", "player", value = sender)
                setVariable("@Context", "context", value = context)
                setVariable("@Args", "args", value = context.args())
            }

            val result = if (force) {
                future.join()
            } else {
                future.getNow(null)
            }

            return when (result) {
                is Array<*> -> result.mapNotNull { it?.toString() }
                is Collection<*> -> result.mapNotNull { it?.toString() }
                else -> if (result != null) listOf(result.toString()) else emptyList()
            }
        }

        if (playerOnly) {
            this.suggestion<Player>(uncheck) { player, context -> handle(player, context) }
        } else {
            this.suggestion<CommandSender>(uncheck) { sender, context -> handle(sender, context) }
        }
    }

    fun CommandComponent.executeScript(source: Any, playerOnly: Boolean, showArgs: Boolean) {
        val builder = StringBuilder()
        val content = buildSection(source, builder).extract()
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        val script = try {
            builder.toString().parseKetherScript()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        if (playerOnly) {
            this.execute<Player> { player, context, argument ->
                script.runActions {
                    setVariable("@Sender", "@Player", "player", value = player)
                    setVariable("@Context", "context", value = context)
                    setVariable("@Arg", "arg", value = argument)
                    setVariable("@Args", "args", value = if (showArgs) context.args() else emptyArray())
                }
            }
        } else {
            this.execute<CommandSender> { sender, context, argument ->
                script.runActions {
                    setVariable("@Sender", value = sender)
                    setVariable("@Context", "context", value = context)
                    setVariable("@Arg", "arg", value = argument)

                    if (sender is Player) setVariable("@Player", "player", value = sender)

                    if (showArgs) {
                        val args = context.args()
                        setVariable("@Args", "args", value = args)
                        for ((i, arg) in args.withIndex()) {
                            set("arg$i", arg)
                        }
                    } else {
                        setVariable("@Args", "args", value = emptyArray<String>())
                    }
                }
            }
        }
    }

    override fun buildSource(): StringBuilder {
        return StringBuilder()
    }

    override fun compileScript() {
    }

    /**
     * 对照并尝试更新
     * */
    fun contrast(config: Configuration) {
        var register = false
        wrapper.updateSource(config).forEach {
            when (it.first) {
                "name", "aliases", "description", "usage",
                "permission", "permission-message", "permission-default" -> register = true
            }
        }

        // 重新注册
        if (register) {
            unregister()
            register()
        }
    }

    companion object {

        val folder = File(getDataFolder(), "commands")

        val cache = mutableMapOf<String, CustomCommand>()

        fun get(id: String): CustomCommand? = cache[id]

        fun onFileChanged(file: File) {
            val start = timing()
            try {
                val id = folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '.')
                val command = cache[id]
                val config = file.toConfig()

                if (config.getBoolean("disable", false)) {
                    // 禁用命令
                    if (command == null) return
                    command.unregister()
                    cache.remove(id)
                }

                if (command != null) {
                    debug(Debug.HIGH, "Command contrasting \"$id\"")
                    command.contrast(config)
                } else {
                    cache[id] = CustomCommand(id, config.wrapper())
                }

                console().sendLang("Custom-Command-Load-Automatic-Succeeded", file.name, timing(start))
            } catch (e: Exception) {
                console().sendLang("Custom-Command-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        fun load(): String {
            val start = timing()
            return try {

                // 注销所有
                cache.values.forEach { it.unregister() }

                // 清除缓存
                cache.clear()

                val folder = folder.toPath()

                if (Files.notExists(folder)) {
                    releaseResourceFile("commands/def.yml", true)
                }

                val iterator = Files.walk(folder).iterator()
                while (iterator.hasNext()) {
                    val path = iterator.next()

                    // 排除
                    if (Files.isDirectory(path)) continue
                    if (path.toString().let { !it.endsWith(".yml") && !it.endsWith("yaml") }) continue
                    if (path.fileName.toString().startsWith("#")) continue

                    val id = folder.relativize(path).toString().replace(File.separatorChar, '.')
                    val config = path.toFile().addWatcher(false) { onFileChanged(this) }.toConfig()
                    if (config.getBoolean("disable", false)) continue
                    cache[id] = CustomCommand(id, config.wrapper())
                }

                console().asLangText("Custom-Command-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Custom-Command-Load-Failed", e.localizedMessage, timing(start)).also {
                    console().sendMessage(it)
                }
            }
        }
    }
}