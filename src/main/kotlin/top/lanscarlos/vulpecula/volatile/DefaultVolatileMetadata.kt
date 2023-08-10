package top.lanscarlos.vulpecula.volatile

import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.volatile
 *
 * @author Lanscarlos
 * @since 2023-08-10 11:10
 */
class DefaultVolatileMetadata : VolatileMetadata {

    private val isUniversal = MinecraftVersion.isUniversal
    private val minecraftVersion = MinecraftVersion.majorLegacy

    override fun createByteMetadata(index: Int, value: Byte): Any {
        return if (isUniversal) {
            val metadata = NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BYTE), value)
            if (minecraftVersion >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.a), value)
        }
    }

    override fun createIntMetadata(index: Int, value: Int): Any {
        return if (isUniversal) {
            val metadata = NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.INT), value)
            if (minecraftVersion >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.b), value)
        }
    }

    override fun createFloatMetadata(index: Int, value: Float): Any {
        return if (isUniversal) {
            val metadata = NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.FLOAT), value)
            if (minecraftVersion >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.c), value)
        }
    }

    override fun createStringMetadata(index: Int, value: String): Any {
        return if (isUniversal) {
            val metadata = NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.STRING), value)
            if (minecraftVersion >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.d), value)
        }
    }

    override fun deconstruct(source: Pair<Int, Any>): Any {
        return when (val value = source.second) {
            is Byte -> createByteMetadata(source.first, value)
            is Int -> createIntMetadata(source.first, value)
            is Float -> createFloatMetadata(source.first, value)
            is String -> createStringMetadata(source.first, value)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.name}")
        }
    }

}

typealias NMSDataWatcherItem<T> = net.minecraft.network.syncher.DataWatcher.Item<T>
typealias NMSDataWatcherObject<T> = net.minecraft.network.syncher.DataWatcherObject<T>
typealias NMSDataWatcherRegistry = net.minecraft.network.syncher.DataWatcherRegistry

typealias NMS16DataWatcherItem<T> = net.minecraft.server.v1_16_R1.DataWatcher.Item<T>
typealias NMS16DataWatcherObject<T> = net.minecraft.server.v1_16_R1.DataWatcherObject<T>
typealias NMS16DataWatcherRegistry = net.minecraft.server.v1_16_R1.DataWatcherRegistry