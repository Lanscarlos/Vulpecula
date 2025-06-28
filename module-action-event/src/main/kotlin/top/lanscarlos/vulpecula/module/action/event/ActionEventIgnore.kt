package top.lanscarlos.vulpecula.module.action.event

import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.event
 *
 * @author Lanscarlos
 * @since 2025/6/18
 */
@BacikalParser("event.ignore")
object ActionEventIgnore : ClassActionResolver {

    fun resolve(frame: BacikalFrame) {
        frame.setVariable("@EVENT_STATUS", "IGNORED")
    }

}