package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:29
 */
object Bacikal {

    lateinit var service: BacikalService

    @Awake(LifeCycle.LOAD)
    fun init() {
        service = DefaultBacikalService
    }

}