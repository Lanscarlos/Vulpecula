package top.lanscarlos.vulpecula

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import top.lanscarlos.vulpecula.internal.*
import top.lanscarlos.vulpecula.internal.ScheduleTask
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.action.ActionUnicode

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
     * @return 返回相关加载信息
     * */
    fun load(loadConfig: Boolean = true): List<String> {

        // 加载主配置
        if (loadConfig) config.reload()

        val messages = mutableListOf<String>()

        // 清理脚本缓存
        KetherShell.mainCache.scriptMap.clear()

        // 加载映射文件
        messages += EventMapper.load()

        // 加载脚本片段
        messages += ScriptFragment.load()

        // 加载 Unicode 映射文件
        if (KetherRegistry.hasAction("unicode")) {
            messages += ActionUnicode.load()
        }

        // 初步加载调度模块
        messages += EventDispatcher.load()

        // 加载处理模块
        messages += EventHandler.load()

        // 调度器处理后续数据
        EventDispatcher.postLoad()

        // 加载日程计划
        messages += ScheduleTask.load()

        return messages
    }

}