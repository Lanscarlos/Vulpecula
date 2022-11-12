package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-12 00:23
 */
class ActionMemory {

    companion object {

        /**
         *
         * 获取变量
         * memory {key}
         * memory {key} -global(g)
         *
         * 设置变量
         * memory {key} to {value}
         * memory {key} to {value} -global(g)
         * */
        @VulKetherParser(
            id = "memory",
            name = ["memory"]
        )
        fun parser() = scriptParser { reader ->
            actionNow {  }
        }
    }
}