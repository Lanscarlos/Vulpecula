package top.lanscarlos.vulpecula.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.module.kether.Workspace
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.utils.timing
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal
 *
 * @author Lanscarlos
 * @since 2022-08-20 12:34
 */
object DispatcherWorkspace {

    private val workspace by lazy {
        val folder = File(getDataFolder(), "compiled")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        Workspace(folder, namespace = listOf("vulpecula"))
    }

    fun load(): String {
        return try {
            val start = timing()
//            workspace.cancelAll()
            workspace.loadAll()
            console().asLangText("Compiled-Load-Succeeded", workspace.scripts.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Compiled-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }
}