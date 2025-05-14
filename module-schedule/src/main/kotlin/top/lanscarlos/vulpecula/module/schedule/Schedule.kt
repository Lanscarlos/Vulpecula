package top.lanscarlos.vulpecula.module.schedule

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/8 15:46
 */
interface Schedule {

    val id: String

    val isAutoStart: Boolean

    val senderSelector: String

    /**
     * 创建日程任务
     *
     * @param id 任务 ID
     * @param senderSelector 脚本执行者选择器
     * @param args 参数
     * */
    fun create(
        id: String = "@",
        senderSelector: String = this@Schedule.senderSelector,
        args: List<String> = emptyList()
    ): ScheduleTask

    /**
     * 创建并启动日程
     *
     * @param id 任务 ID
     * @param senderSelector 脚本执行者选择器
     * @param args 参数
     * @return 日程任务
     * */
    fun start(
        id: String = "~",
        senderSelector: String = this@Schedule.senderSelector,
        args: List<String> = emptyList()
    ): ScheduleTask {
        return create(id, senderSelector, args).also(ScheduleTask::start)
    }

    /**
     * 终止日程
     *
     * @param pid 任务 ID, 若为 -1 则代表所有任务
     * */
    fun stop(pid: Long)

    /**
     * 终止日程
     *
     * @param id 任务 PID, 若为 * 则代表所有任务
     * */
    fun stop(id: String)

}