package top.lanscarlos.vulpecula.module.schedule

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/12 15:18
 */
enum class TaskState(val isRunning: Boolean) {

    WAITING(true), // 等待运行
    RUNNING(true), // 正在运行
    PAUSED(false), // 已暂停
    TERMINATED(false); // 已终止

}