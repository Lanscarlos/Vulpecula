package top.lanscarlos.vulpecula.modularity

import taboolib.common.platform.command.PermissionDefault

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-09-02 10:50
 */
interface ModularCommand : ModularComponent {

    /**
     * 命令名称
     * */
    val name: String

    /**
     * 命令别名
     * */
    val aliases: List<String>

    /**
     * 命令描述
     * */
    val description: String

    /**
     * 命令用法
     * */
    val usage: String

    /**
     * 命令权限
     * */
    val permission: String

    /**
     * 命令权限消息
     * */
    val permissionMessage: String

    /**
     * 命令权限默认值
     * */
    val permissionDefault: PermissionDefault

    /**
     * 是否使用新解析器
     * */
    val useParser: Boolean

    /**
     * 注册命令
     * */
    fun register()

    /**
     * 注销命令
     * */
    fun unregister()
}