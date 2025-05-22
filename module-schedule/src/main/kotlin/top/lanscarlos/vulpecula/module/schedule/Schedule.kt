package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.platform.ProxyCommandSender

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

    val tasks: HashMap<String, out ScheduleTask>

    /**
     * 创建日程任务
     *
     * @param id 任务 ID
     * @param sender 脚本执行者
     * @param args 参数
     * */
    fun create(
        pid: String = "~",
        sender: ProxyCommandSender? = null,
        args: List<String> = emptyList()
    ): ScheduleTask

    /**
     * 创建并启动日程
     *
     * @param id 任务 ID
     * @param args 参数
     * @return 日程任务
     * */
    fun start(
        pid: String = "~",
        sender: ProxyCommandSender? = null,
        args: List<String> = emptyList()
    ): ScheduleTask {
        return create(pid, sender, args).also(ScheduleTask::start)
    }

    /**
     * 终止日程
     *
     * @param pid 任务 PID, 若为 * 则代表所有任务
     * */
    fun stop(pid: String)

}