package top.lanscarlos.vulpecula.module.schedule

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025-05-11 09:42
 */
interface ScheduleTask {

    val pid: Int

    val state: TaskState

    val activationTime: Long

    val expirationTime: Long

    val counter: Int

    val isOutOfDuration: Boolean

    val isOutOfMaxRuns: Boolean

    /**
     * 开始
     * */
    fun start()

    /**
     * 暂停
     * */
    fun pause()

    /**
     * 恢复
     * */
    fun resume()

    /**
     * 终止
     * */
    fun stop()

}