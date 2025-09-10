package top.lanscarlos.vulpecula.module.action.target

import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.target
 *
 * @author Lanscarlos
 * @since 2025/9/3
 */
@Parser("target.select.self")
object ActionTargetSelectSelf : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame
    ) {
        ActionTarget.setContext(frame, frame.senderAsPlayer ?: return)
    }

}