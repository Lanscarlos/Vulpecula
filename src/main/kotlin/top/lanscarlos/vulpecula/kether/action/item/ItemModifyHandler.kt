package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readItemStack
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:32
 */

@SuppressWarnings("deprecation")
object ItemModifyHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("modify", "set")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        val options = mutableMapOf<String, LiveData<*>>()
        while (reader.nextPeek().startsWith('-')) {
            when (val it = reader.nextToken().substring(1)) {
                "material", "mat", "type" -> options["material"] = StringLiveData(reader.nextBlock())
                "amount", "amt", "a" -> options["amount"] = reader.readInt()
                "durability", "dura" -> options["durability"] = reader.readInt()
                "name", "n" -> options["name"] = StringLiveData(reader.nextBlock())
                "customModelData", "model" -> options["model"] = reader.readInt()
                else -> error("Unknown argument \"$it\" at item build action.")
            }
        }

        return acceptTransferFuture(source) { item ->
            options.mapValues { it.value.getOrNull(this) }.thenTake().thenApply { args ->
                val meta = item.itemMeta
                for (option in args) {
                    when (option.key) {
                        "material" -> {
                            val mat = option.value?.toString()?.uppercase() ?: continue
                            item.type = XMaterial.matchXMaterial(mat).let {
                                if (it.isPresent) it.get().parseMaterial() else null
                            } ?: item.type
                        }
                        "amount" -> item.amount = option.value.coerceInt(item.amount)
                        "durability" -> item.durability = option.value.coerceShort(item.durability)
                        "name" -> {
                            val name = option.value?.toString()
                            meta?.setDisplayName(name?.colored())
                        }
                        "model" -> {
                            val customModelData = option.value.coerceInt(-1)
                            try {
                                if (customModelData != -1) {
                                    meta?.invokeMethod<Void>("setCustomModelData", customModelData)
                                }
                            } catch (ignored: NoSuchMethodException) {
                            }
                        }
                    }
                }

                item.also { it.itemMeta = meta }
            }
        }
    }
}