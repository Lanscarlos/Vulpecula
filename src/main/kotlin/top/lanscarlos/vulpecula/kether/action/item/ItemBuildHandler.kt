package top.lanscarlos.vulpecula.kether.action.item

import taboolib.library.kether.QuestReader
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.kether.live.readBoolean
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:28
 */
@Deprecated("")
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

        return transferFuture {
            return@transferFuture listOf(
                type.getOrNull(this),
                options["amount"]?.getOrNull(this),
                options["durability"]?.getOrNull(this),
                options["name"]?.getOrNull(this),
                options["shiny"]?.getOrNull(this),
                options["colored"]?.getOrNull(this),
                options["model"]?.getOrNull(this)
            ).thenTake().thenApply { args ->
                val material = XMaterial.matchXMaterial(args[0]?.toString()?.uppercase() ?: "STONE").let {
                    if (it.isPresent) it.get() else XMaterial.STONE
                }

                buildItem(material) {
                    args[1]?.toString()?.coerceInt()?.let { amount = it }
                    args[2]?.toString()?.coerceInt()?.let { damage = it }
                    args[3]?.toString()?.let { name = it }
                    args[4]?.toString()?.coerceBoolean()?.let { if (it) shiny() }
                    args[5]?.toString()?.coerceBoolean()?.let { if (it) colored() }
                    args[6]?.toString()?.coerceInt()?.let { customModelData = it }
                }
            }
        }
    }
}