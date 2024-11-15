package top.lanscarlos.vulpecula.wireshark

import taboolib.common.platform.function.info
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.NMSItemTag
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.wireshark
 *
 * @author Lanscarlos
 * @since 2024-11-10 17:12
 */
class ClientboundLevelChunkWithLightPacketMapper(val source: Any) {

    val handler = nmsProxy<NMSItemTag>()

    val x = source.getProperty<Int>("x")!!

    val z = source.getProperty<Int>("z")!!

    val tag = handler.itemTagToString(handler.itemTagToBukkitCopy(source.getProperty<Any>("chunkData/b")!!))

    val blocks = mutableListOf<String>()

    init {
        val data = source.getProperty<Any>("chunkData/d")!!
        info("data. ${data.javaClass.name}")
        val list = (data as List<*>)
        if (list.isEmpty()) {
            info("list. empty")
        }
        for (element in list) {
            if (element == null) {
                info("element. null")
                continue
            }
            val a = element.getProperty<Int>("a")!!
            val b = element.getProperty<Int>("b")!!
            val tag = handler.itemTagToString(handler.itemTagToBukkitCopy(element.getProperty<Any>("d")!!))
            blocks.add("a=$a, b=$b >> $tag")
        }
    }

}