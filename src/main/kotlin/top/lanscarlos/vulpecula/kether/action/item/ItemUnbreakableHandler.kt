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

    override fun read(isRoot: Boolean, reader: QuestReader): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.hasNextToken("to")
        val unbreakable = reader.readBoolean()

        return applyTransfer(source) { _, meta ->
            val isUnbreakable = unbreakable.get(this, false)

            try {
                meta.isUnbreakable = isUnbreakable
            } catch (ex: NoSuchMethodError) {
                try {
                    meta.invokeMethod<Any>("spigot")!!.invokeMethod<Any>("setUnbreakable", isUnbreakable)
                } catch (ignored: NoSuchMethodException) {
                }
            }

            return@applyTransfer meta
        }
    }
}