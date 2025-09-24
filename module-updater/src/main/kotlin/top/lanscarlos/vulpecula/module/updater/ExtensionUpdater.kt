package top.lanscarlos.vulpecula.module.updater

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerLifeCycleTask
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.updater
 *
 * @author Lanscarlos
 * @since 2025/7/4 11:02
 */
object ExtensionUpdater {

    private val updateFolder by lazy { File(getDataFolder(), "updater") }

    private val extensionFolder by lazy { File(getDataFolder(), "extension") }

    private val artifacts: HashMap<String, Artifact> = hashMapOf()

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 5, runnable = ::scanUpdate)
    }

    /**
     * 检索新的版本
     * */
    fun scanUpdate() {
        if (!updateFolder.exists()) {
            info("未发现需要更新的内容")
            return
        }
        info("尝试更新插件主体")

        // 载入需要更新的包体
        for (file in updateFolder.listFiles()) {
            val artifact = Artifact(file)
            if (!artifacts.containsKey(artifact.name)) {
                artifacts[artifact.name] = artifact
                continue
            }
            if (artifact > artifacts[artifact.name]!!) {
                artifacts[artifact.name] = artifact
            }
        }

        if (artifacts.isEmpty()) {
            // 无更新内容
            return
        }

        // 执行更新
        if (!extensionFolder.exists()) {
            extensionFolder.mkdirs()
        }
        for (file in File(getDataFolder(), "extension").listFiles()) {
            val oldArtifact = Artifact(file)
            val newArtifact = artifacts[oldArtifact.name] ?: continue
            if (newArtifact <= oldArtifact) {
                // 版本过小
                continue
            }
            // 执行更新
            Files.move(
                newArtifact.file.toPath(),
                oldArtifact.file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            info("成功更新 ${newArtifact.name} 扩展, ${oldArtifact.version} -> ${newArtifact.version}")
        }
        info("扩展更新完成.")
    }

}