package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-12 00:26
 */
class ActionYaml {

    companion object {

        /**
         *
         * 打开一个 Yaml 文件
         * yaml open {path}
         *
         * 获取数据
         * yaml get {path}
         *
         * 设置数据
         * yaml set {path} to {value}
         *
         * 保存文件
         * yaml save {yaml} to {file}
         *
         * */
        @VulKetherParser(
            id = "yaml",
            name = ["yaml", "yml"]
        )
        fun parser() = scriptParser { reader ->
            actionNow {  }
        }
    }
}