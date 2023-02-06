package top.lanscarlos.vulpecula

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import top.lanscarlos.vulpecula.internal.CustomCommand
import top.lanscarlos.vulpecula.internal.*
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.action.vulpecula.ActionUnicode
import top.lanscarlos.vulpecula.script.VulScript
import top.lanscarlos.vulpecula.script.ScriptWorkspace

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

    /**
     * @param init 是否为初始化操作
     * @return 返回相关加载信息
     * */
    fun load(init: Boolean = false): List<String> {

        // 重载主配置
        if (!init) config.reload()

        val messages = mutableListOf<String>()

        // 清理脚本缓存
        KetherShell.mainCache.scriptMap.clear()

        // 注销所有监听器
        EventListener.unregisterAll()

        // 加载映射文件
        messages += EventMapper.load()

        // 加载 Unicode 映射文件
        if (KetherRegistry.hasAction("unicode")) {
            messages += ActionUnicode.load()
        }

        // 加载脚本
        messages += VulScript.load()
        messages += ScriptWorkspace.load()

        // 加载自定义命令
        messages += CustomCommand.load()

        // 初步加载调度模块
        messages += EventDispatcher.load()

        // 加载处理模块
        messages += EventHandler.load()

        // 调度器处理后续数据
        EventDispatcher.postLoad()

        // 加载日程计划
        messages += ScheduleTask.load(init)

        return messages
    }

}