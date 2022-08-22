package top.lanscarlos.vulpecula

import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import top.lanscarlos.vulpecula.internal.*
import top.lanscarlos.vulpecula.utils.Debug

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-08-19 17:44
 */
object VulpeculaContext {

    /**
     * @return 返回相关加载信息
     * */
    fun load(config: Configuration): List<String> {
        val messages = mutableListOf<String>()

        // 清理脚本缓存
        KetherShell.mainCache.scriptMap.clear()

        // 加载调试模块
        Debug.load(config)

        // 加载映射文件
        messages += EventMapping.load()

        // 加载脚本片段
        messages += ScriptFragment.load()

        // 初步加载调度模块
        messages += EventDispatcher.preLoad()

        // 加载处理模块
        messages += EventHandler.load()

        // 调度器处理后续数据
        EventDispatcher.postLoad()

        // 注册监听器
        EventListener.registerAll()

        return messages
    }

}

typealias Context = VulpeculaContext