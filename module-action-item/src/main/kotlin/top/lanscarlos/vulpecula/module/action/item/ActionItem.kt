package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
object ActionItem {

    private const val CONTEXT = "@ITEM"

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        info("我擦测试！！！")
    }

    fun getItem(frame: BacikalFrame): ItemStack {
        return frame.getVariable<ItemStack>(CONTEXT) ?: error(asLang("module-item-exception-null-item"))
    }

    fun setItem(frame: BacikalFrame, item: ItemStack) {
        frame.setVariable(CONTEXT, item)
    }

}