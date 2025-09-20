package top.lanscarlos.vulpecula.module.updater

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerLifeCycleTask
import java.io.File
import java.util.jar.JarFile

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.updater
 *
 * @author Lanscarlos
 * @since 2025/7/4 11:02
 */
object ExtensionUpdater {

    private val folder by lazy { File(getDataFolder(), "updater") }

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 5, runnable = ::scanUpdate)
    }

    /**
     * 检索新的版本
     * */
    fun scanUpdate() {
        if (!folder.exists()) {
            info("未发现需要更新的内容")
            return
        }
        info("尝试更新插件主体")
        for (file in folder.listFiles()) {
            val jarFile = JarFile(file).getJarEntry("plugin.yml")
        }
    }

}