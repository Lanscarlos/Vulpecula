package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.registerLifeCycleTask

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/6/16
 */
object ActionLoader {

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 1) {
            // TODO 注册语句
        }
    }

}