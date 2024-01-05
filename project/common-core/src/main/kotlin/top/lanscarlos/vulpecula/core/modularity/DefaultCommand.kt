package top.lanscarlos.vulpecula.core.modularity

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.function.registerCommand
import taboolib.common.platform.function.unregisterCommand
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.bacikal.bacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalBlockBuilder
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.DefaultBlockBuilder
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.modularity.ModularCommand
import top.lanscarlos.vulpecula.modularity.Module
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2024-01-05 12:23
 */
class DefaultCommand(
    override val module: Module,
    override val id: String,
    val config: DynamicConfig
) : ModularCommand, Runnable {

    override val file: File
        get() = config.file

    override val name: String by config.read("name") {
        it?.toString() ?: error("Invalid name at command \"$id\"")
    }

    override val aliases: List<String> by config.readStringList("aliases", emptyList())

    override val description: String by config.readString("description", "")

    override val usage: String by config.readString("usage", "")

    override val permission: String by config.readString("permission", "")

    override val permissionMessage: String by config.readString("permission-message", "")

    override val permissionDefault: PermissionDefault by config.read("permission-default") { value ->
        PermissionDefault.values().firstOrNull {
            it.name.equals(value?.toString(), true)
        } ?: PermissionDefault.OP
    }

    override val useParser: Boolean by config.readBoolean("use-parser", false)

    /**
     * 自动重载
     * */
    private val automaticReload: Boolean by config.readBoolean("$id.automatic-reload", true)

    /**
     * 指令结构
     * */
    val structure: Map<String, DefaultCommandBuilder> by config.read("structure") { value ->
        val section = value as? ConfigurationSection ?: error("Invalid structure at command \"$id\"")
        val nodes = mutableMapOf<String, DefaultCommandBuilder>()

        // 加载所有节点
        for (key in section.getKeys(false)) {
            val node = section.getConfigurationSection(key) ?: continue
            nodes[key] = DefaultCommandBuilder(this, key, node)
        }

        // 处理父子节点关系
        for (builder in nodes.values) {
            val parent = builder.section.getString("parent") ?: "main"
            nodes[parent]?.children?.plusAssign(builder)
        }

        nodes
    }

    /**
     * 公共函数库
     * */
    val functions: Map<String, BacikalBlockBuilder> by config.read("functions") { value ->
        val section = value as? ConfigurationSection ?: return@read emptyMap()
        val functions = mutableMapOf<String, BacikalBlockBuilder>()

        for (key in section.getKeys(false)) {
            functions[key] = DefaultBlockBuilder(key, section[key] ?: continue)
        }

        functions
    }

    /**
     * 根节点
     * */
    private var root: CommandBase

    var quest: BacikalQuest
        private set

    init {
        config.onAfterReload(this)

        // 构建脚本
        quest = bacikalQuest {
            for ((key, node) in structure) {
                node.section["execute"]?.let { appendBlock("@$key", it) }
            }
            for (function in functions.values) {
                appendBlock(function)
            }
        }

        // 构建命令
        root = structure["main"]?.build(-1) as? CommandBase ?: error("Invalid main structure at command \"$id\"")
        register()

        if (automaticReload) {
            FileWatcher.INSTANCE.addSimpleListener(file) {
                config.reload()
            }
        }
    }

    override fun run() {
        unregister()

        // 构建脚本
        quest = bacikalQuest {
            for ((key, node) in structure) {
                node.section["execute"]?.let { appendBlock("@$key", it) }
            }
            for (function in functions.values) {
                appendBlock(function)
            }
        }

        // 构建命令
        root = structure["main"]?.build(-1) as? CommandBase ?: error("Invalid main structure at command \"$id\"")
        register()

        // 检查文件自动重载
        if (automaticReload) {
            FileWatcher.INSTANCE.addSimpleListener(file) {
                config.reload()
            }
        } else if (FileWatcher.INSTANCE.hasListener(file)) {
            // 移除自动重载
            FileWatcher.INSTANCE.removeListener(file)
        }
    }

    override fun register() {
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
                    return root.execute(CommandContext(sender, command, name, root, useParser, args))
                }
            },
            // 创建补全器
            completer = object : CommandCompleter {
                override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                    return root.suggest(CommandContext(sender, command, name, root, useParser, args))
                }
            },
            // 传入原始命令构建器
            commandBuilder = {}
        )
    }

    override fun unregister() {
        if (aliases.isNotEmpty()) aliases.forEach { unregisterCommand(it) }
        unregisterCommand(name)
    }
}