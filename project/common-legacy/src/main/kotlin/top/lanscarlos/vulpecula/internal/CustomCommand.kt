package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.*
import taboolib.common5.cbool
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.bindConfigNode
import top.lanscarlos.vulpecula.config.DynamicConfig.Companion.toDynamic
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
    val wrapper: DynamicConfig
) {

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
    val newParser by wrapper.readBoolean("enable-parser", false)
    val main by wrapper.read("main") { section ->
        section as? ConfigurationSection
    }
    val components by wrapper.read("components") { section ->
        section as? ConfigurationSection
    }

    lateinit var root: CommandBase

    init {
        // 先加载 main 后加载 components
        buildCommand()
        register()
    }

    private fun buildCommand() {

        val root = CommandComponentBuilder("main", main ?: Configuration.empty())
        val section = components ?: let {
            // 没有 components 节点，直接构建 root 节点
            this.root = root.build(-1) as CommandBase
            return
        }

        val loaded = mutableMapOf<String, CommandComponentBuilder>()

        // 加载所有节点
        for (key in section.getKeys(false)) {
            val node = section.getConfigurationSection(key) ?: continue
            loaded[key] = CommandComponentBuilder(key, node)
        }

        // 处理父子节点关系
        for (builder in loaded.values) {
            val parent = builder.section.getString("parent") ?: "main"

            if (parent == "main") {
                root.children += builder
            } else {
                loaded[parent]?.children?.plusAssign(builder)
            }
        }

        this.root = root.build(-1) as CommandBase
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
                    return root.execute(CommandContext(sender, command, name, root, newParser, args))
                }
            },
            // 创建补全器
            completer = object : CommandCompleter {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                    return root.suggest(CommandContext(sender, command, name, root, newParser, args))
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

    /**
     * 对照并尝试更新
     * */
    fun contrast(config: Configuration) {
        var register = false
        var rebuild = false
        wrapper.updateSource(config).forEach {
            when (it.first) {
                "name", "aliases", "description", "usage",
                "permission", "permission-message", "permission-default" -> register = true
                "main", "components" -> rebuild = true
            }
        }

        // 重构
        if (rebuild) {
            buildCommand()
        }

        // 重新注册
        if (register) {
            unregister()
            register()
        }
    }

    companion object {

        val automaticReload by bindConfigNode("automatic-reload.custom-command") {
            it?.cbool ?: false
        }

        val folder = File(getDataFolder(), "commands")

        val cache = mutableMapOf<String, CustomCommand>()

        fun get(id: String): CustomCommand? = cache[id]

        fun onFileChanged(file: File) {
            if (!automaticReload) {
                file.removeWatcher()
                return
            }

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
                    cache[id] = CustomCommand(id, config.toDynamic())
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
                    releaseResourceFile("commands/#def.yml", true)
                }

                val iterator = Files.walk(folder).iterator()
                while (iterator.hasNext()) {
                    val path = iterator.next()

                    // 排除
                    if (Files.isDirectory(path)) continue
                    if (path.toString().let { !it.endsWith(".yml") && !it.endsWith("yaml") }) continue
                    if (path.fileName.toString().startsWith("#")) continue

                    val id = folder.relativize(path).toString().replace(File.separatorChar, '.')
                    val config = path.toFile().apply {
                        if (automaticReload) addWatcher(false) { onFileChanged(this) }
                    }.toConfig()
                    if (config.getBoolean("disable", false)) continue
                    cache[id] = CustomCommand(id, config.toDynamic())
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