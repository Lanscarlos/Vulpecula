package top.lanscarlos.vulpecula.modularity

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-09-02 10:38
 */
interface ModularSchedule : ModularComponent {

    /**
     * 任务周期
     * */
    val period: Long

    /**
     * 延迟时间
     * */
    val delay: Long

    /**
     * 起始时间
     * */
    val startTime: Long

    /**
     * 结束时间
     * */
    val endTime: Long

    /**
     * 是否自动启动
     * 即在服务器进入 ACTIVE 状态时自动启动
     * */
    val autostart: Boolean

    /**
     * 是否在异步线程运行
     * */
    val isAsync: Boolean

    /**
     * 是否正在运行
     * */
    val isRunning: Boolean

    /**
     * 启动任务
     * */
    fun start()

    /**
     * 停止任务
     * */
    fun stop()
}