package top.lanscarlos.vulpecula.action.item

import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.bacikal.parser.BacikalActionParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalComplexActionParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParser
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.action.item
 *
 * @author Lanscarlos
 * @since 2024-05-13 17:13
 */
@BacikalParser("item")
object ActionItem : BacikalComplexActionParser("item")