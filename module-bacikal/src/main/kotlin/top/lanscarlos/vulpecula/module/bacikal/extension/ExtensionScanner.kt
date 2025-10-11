package top.lanscarlos.vulpecula.module.bacikal.extension

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.releaseResourceFolder
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.BacikalScanner
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.extension
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
object ExtensionScanner {

    val archiveFolder: File by lazy { File(getDataFolder(), "extension/archive") }

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 6, runnable = ::scanExtension)
    }

    /**
     * 扫描拓展包
     * */
    private fun scanExtension() {
        val folder = File(getDataFolder(), "extension")
        if (!folder.exists()) {
            releaseResourceFolder("extension")
        }

        // 执行更新
        update(folder)

        // 获取所有扩展包体
        val artifacts: Map<String, Artifact> = getArtifacts(folder)
        for (artifact in artifacts.values) {
            val extension = ExternalExtension(artifact)

            // 遍历 class 对象
            for (owner in extension.classes.values) {
                BacikalScanner.visitClass(owner, extension)
            }

            BacikalRegistry.registerExtension(extension)
        }
    }

    private fun update(workspace: File) {
        // 处理自动更新
        val folder = File(workspace, "updater")
        if (!folder.exists()) {
            folder.mkdirs()
            return
        }

        // 获取所有扩展包体
        val artifacts: Map<String, Artifact> = getArtifacts(folder)

        // 扫描并执行更新
        for (file in workspace.listFiles()) {
            if (!file.exists() || !file.isFile || !file.canRead() || file.extension != "jar") {
                continue
            }
            val oldArtifact = Artifact(file)
            val newArtifact = artifacts[oldArtifact.name] ?: continue

            // 对旧的包体进行归档
            archive(oldArtifact)

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

    /**
     * 获取当前文件夹下所有的扩展包体
     * */
    private fun getArtifacts(folder: File): Map<String, Artifact> {
        val artifacts: HashMap<String, Artifact> = hashMapOf()
        for (file in folder.listFiles()) {
            if (!file.exists() || !file.isFile || !file.canRead() || file.extension != "jar") {
                continue
            }
            val artifact = Artifact(file)
            if (!artifacts.containsKey(artifact.name)) {
                artifacts[artifact.name] = artifact
                continue
            }
            val oldArtifact = artifacts[artifact.name]!!
            if (artifact > oldArtifact) {
                info("${artifact.file.name} > ${oldArtifact.file.name}")
                artifacts[artifact.name] = artifact
                archive(oldArtifact)
            } else {
                info("${oldArtifact.file.name} > ${artifact.file.name}")
                archive(artifact)
            }
        }
        return artifacts
    }

    /**
     * 归档扩展包
     * */
    private fun archive(artifact: Artifact) {
        val target = File(getDataFolder(), "extension/archive/${artifact.file.name}")
        if (!target.parentFile.exists()) {
            target.parentFile.mkdirs()
        }
        Files.move(
            artifact.file.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        )
    }

}