package top.lanscarlos.vulpecula.common.config

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * 文件监听
 *
 * @author Lanscarlos
 * @since 2025/4/25 14:06
 */
interface ConfigServiceCallback {

    fun onFileCreated(sender: ProxyCommandSender, id: String, file: File)

    fun onFileModified(sender: ProxyCommandSender, id: String, file: File)

    fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File)

    fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Exception)

    /**
     * 当路径不存在时调用
     * */
    fun onLoadInit(sender: ProxyCommandSender, directory: File)

    fun onLoadStarted(sender: ProxyCommandSender) {}

    fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double)

    /**
     * 加载成功时调用
     *
     * @param created 新增文件数
     * @param modified 修改的文件数
     * @param deleted 移除的文件数
     * @param failed 异常的文件数
     * @param time 耗时, 单位毫秒
     * */
    fun onLoadSuccess(sender: ProxyCommandSender, created: Int, modified: Int, deleted: Int, failed: Int, time: Double)

    fun onLoadFailure(sender: ProxyCommandSender, time: Double, e: Throwable)

}