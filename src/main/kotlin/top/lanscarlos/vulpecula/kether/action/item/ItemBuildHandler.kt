package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:28
 */
object ItemBuildHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("build")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {

        val type = StringLiveData(reader.nextBlock())
        val options = mutableMapOf<String, LiveData<*>>()
        while (reader.nextPeek().startsWith('-')) {
            when (val it = reader.nextToken().substring(1)) {
                "amount", "amt", "a" -> options["amount"] = reader.readInt()
                "durability", "dura" -> options["durability"] = reader.readInt()
                "name", "n" -> options["name"] = StringLiveData(reader.nextBlock())
                "shiny" -> options["shiny"] = reader.readBoolean()
                "colored" -> options["colored"] = reader.readBoolean()
                "customModelData", "model" -> options["model"] = reader.readInt()
                else -> error("Unknown argument \"$it\" at item build action.")
            }
        }

        return transfer {
            val material = XMaterial.matchXMaterial(type.get(this, "STONE").uppercase()).let {
                if (it.isPresent) it.get() else XMaterial.STONE
            }
            buildItem(material) {
                for (option in options) {
                    when (option.key) {
                        "amount" -> amount = option.value.getValue(this@transfer, amount)
                        "durability" -> damage = option.value.getValue(this@transfer, damage)
                        "name" -> name = option.value.getValueOrNull<String>(this@transfer) ?: name
                        "shiny" -> {
                            if (option.value.getValue(this@transfer, false)) shiny()
                        }
                        "colored" -> {
                            if (option.value.getValue(this@transfer, true)) colored()
                        }
                        "model" -> customModelData = option.value.getValue(this@transfer, customModelData)
                    }
                }
                colored()
            }
        }
    }
}