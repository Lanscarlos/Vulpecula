package top.lanscarlos.vulpecula.module.schedule

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/12 15:18
 */
enum class TaskState {

    WAITING, // 等待运行
    RUNNING, // 正在运行
    PAUSED, // 已暂停
    TERMINATED; // 已终止

}