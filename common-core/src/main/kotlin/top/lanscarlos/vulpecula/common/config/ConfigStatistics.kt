package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.utils.TimeUtil
import java.io.File
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025/11/23
 */
class ConfigStatistics(val startTime: Long) {

    val scannedFiles: HashSet<File> = HashSet()

    val unmodifiedFiles: HashSet<File> = HashSet()

    val createdFiles: HashSet<File> = HashSet()

    val modifiedFiles: HashSet<File> = HashSet()

    val deletedFiles: HashSet<File> = HashSet()

    val failedFiles: HashSet<File> = HashSet()

    val consumeTime: Double by lazy { TimeUtil.stopTiming(startTime) }

    /**
     * 用于展示的详情信息
     */
    val displayDetails: LinkedList<String> = LinkedList()

}