package top.lanscarlos.vulpecula.module.action.item

import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
@Parser("item.amount.maximum", aliases = ["max"])
object ActionItemAmountMaximum : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.type.maxStackSize
    }

}

@Parser("item.amount.get")
object ActionItemAmountGet : ClassActionResolver {

    fun resolve(frame: BacikalFrame): Int {
        val item = ActionItem.getContext(frame)
        return item.amount
    }

}

@Parser("item.amount.set")
object ActionItemAmountSet : ClassActionResolver {

    fun resolve(frame: BacikalFrame, amount: Int) {
        val item = ActionItem.getContext(frame)
        require(amount in 1..item.type.maxStackSize) {
            Lang.ACTION_ITEM_EXCEPTION_INVALID_AMOUNT.asText(console(), amount)
        }
        item.amount = amount
    }

}

@Parser("item.amount.increase", aliases = ["inc", "add"])
object ActionItemAmountIncrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, amount: Int) {
        val item = ActionItem.getContext(frame)
        item.amount = (item.amount + amount).coerceAtMost(item.type.maxStackSize)
    }

}

@Parser("item.amount.decrease", aliases = ["dec"])
object ActionItemAmountDecrease : ClassActionResolver {

    fun resolve(frame: BacikalFrame, amount: Int) {
        val item = ActionItem.getContext(frame)
        item.amount = (item.amount - amount).coerceAtLeast(1)
    }

}