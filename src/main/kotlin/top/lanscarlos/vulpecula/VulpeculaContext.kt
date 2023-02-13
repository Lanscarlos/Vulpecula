package top.lanscarlos.vulpecula

import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.internal.CustomCommand
import top.lanscarlos.vulpecula.internal.*
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.kether.action.vulpecula.ActionUnicode
import top.lanscarlos.vulpecula.internal.VulScript
import top.lanscarlos.vulpecula.internal.ScriptWorkspace
import top.lanscarlos.vulpecula.utils.timing

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-08-19 17:44
 */
object VulpeculaContext {

    @Config
    lateinit var config: Configuration
        private set

    fun loadConfig(): String {
        val start = timing()
        return try {
            config.reload()
            console().asLangText("Config-Load-Succeeded", timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            console().asLangText("Config-Load-Failed", e.localizedMessage, timing(start)).also {
                console().sendMessage(it)
            }
        }
    }

    /**
     * @param init 是否为初始化操作
     * @return 返回相关加载信息
     * */
    fun load(init: Boolean = false): List<String> {

        val messages = mutableListOf<String>()

        // 重载主配置
        if (!init) {
            messages += loadConfig()
        }

        // 清理脚本缓存
        KetherShell.mainCache.scriptMap.clear()

        // 注销所有监听器
        EventListener.unregisterAll()

        // 加载映射文件
        messages += EventMapper.load()

        // 加载 Unicode 映射文件
        if (ActionUnicode.enable) {
            messages += ActionUnicode.load()
        }

        // 加载脚本
        messages += VulScript.load()
        messages += ScriptWorkspace.load()

        // 初步加载调度模块
        messages += EventDispatcher.load()

        // 加载处理模块
        messages += EventHandler.load()

        // 调度器处理后续数据
        EventDispatcher.postLoad()

        // 加载自定义命令
        messages += CustomCommand.load()

        // 加载日程计划
        messages += ScheduleTask.load(init)

        return messages
    }

}