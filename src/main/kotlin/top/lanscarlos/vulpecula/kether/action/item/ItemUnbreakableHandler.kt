package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.readBoolean
import top.lanscarlos.vulpecula.utils.readItemStack

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:40
 */
object ItemUnbreakableHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("unbreakable", "unbreak")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.hasNextToken("to")
        val unbreakable = reader.readBoolean()

        return acceptTransferFuture(source) { item ->
            unbreakable.get(this, false).thenApply { isUnbreakable ->
                val meta = item.itemMeta ?: return@thenApply item

                try {
                    meta.isUnbreakable = isUnbreakable
                } catch (ex: NoSuchMethodError) {
                    try {
                        meta.invokeMethod<Any>("spigot")!!.invokeMethod<Any>("setUnbreakable", isUnbreakable)
                    } catch (ignored: NoSuchMethodException) {
                        // 忽略
                    }
                }

                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }
}