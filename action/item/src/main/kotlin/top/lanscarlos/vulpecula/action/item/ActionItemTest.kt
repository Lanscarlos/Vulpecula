package top.lanscarlos.vulpecula.action.item

import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParserBody

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.action.item
 *
 * @author Lanscarlos
 * @since 2024-05-14 23:37
 */
@BacikalParserBody("test", "item")
object ActionItemTest : BacikalActionParser() {

    fun resolve(
        name: String,
        @Optional(["amt"]) amount: Int = 32
    ): String {
        info("running item $name and $amount.")
        return "item $name $amount"
    }

}