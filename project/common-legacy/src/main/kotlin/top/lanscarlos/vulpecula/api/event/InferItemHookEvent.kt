package top.lanscarlos.vulpecula.api.event

import taboolib.platform.type.BukkitProxyEvent
import top.lanscarlos.vulpecula.api.chemdah.InferItem

/**
 * Chemdah
 * ink.ptms.chemdah.api.event.InferItemHookEvent
 *
 * @author sky
 * @since 2021/4/17 2:41 下午
 */
class InferItemHookEvent(val id: String, var itemClass: Class<out InferItem.Item>) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false
}