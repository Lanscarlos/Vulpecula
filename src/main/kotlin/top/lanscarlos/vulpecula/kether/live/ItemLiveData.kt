package top.lanscarlos.vulpecula.kether.live

import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.library.xseries.XMaterial
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.utils.nextBlock
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-12 14:24
 */
class ItemLiveData(
    val value: Any
) : LiveData<ItemStack> {

    override fun get(frame: ScriptFrame, def: ItemStack): CompletableFuture<ItemStack> {
        return getOrNull(frame).thenApply { if (it != null) def else def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<ItemStack?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                is ItemStack -> it
                is Item -> it.itemStack
                is String -> {
                    val material = XMaterial.matchXMaterial(it.uppercase()).let { mat ->
                        if (mat.isPresent) mat.get() else return@thenApply null
                    }
                    buildItem(material)
                }
                else -> null
            }
        }
    }
}