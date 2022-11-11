package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.nextPeek

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-12 00:01
 */
class ActionFunction {

    companion object {

        /**
         *
         * fun import xxx
         * fun name {...}
         * fun name ( arg1 arg2 = xxx arg3... ) {...}
         *
         * */
        @VulKetherParser(
            id = "function",
            name = ["function", "fun"]
        )
        fun parser() = scriptParser { reader ->
            if (reader.hasNextToken("import")) {
                // 函数导入
            }

            val name = reader.nextToken()

            if (reader.hasNextToken("(")) {
                // 有参数
                while (!reader.hasNextToken(")")) {
                    // 读取参数
                }
            } else {
                // 跳过空括号
                reader.hasNextToken("()")
            }

            // 判断方法体前缀
            if (reader.nextPeek() != "{") {
                // 不合法的方法体
                error("Should be entered \"{\" is actually ${reader.nextPeek()}")
            }

            // 读取方法体
            val body = reader.nextBlock()

            actionNow { null }
        }
    }
}