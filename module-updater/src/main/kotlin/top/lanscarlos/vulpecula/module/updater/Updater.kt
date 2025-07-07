package top.lanscarlos.vulpecula.module.updater

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.updater
 *
 * @author Lanscarlos
 * @since 2025/7/4 11:02
 */
object Updater {

    private val folder by lazy { File(getDataFolder(), "updater") }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        if (!folder.exists()) {
            info("未发现需要更新的内容")
            return
        }
        info("尝试更新插件主体")

    }

    /**
     * 检索新的版本
     * */
    fun scanUpdate() {

    }

}