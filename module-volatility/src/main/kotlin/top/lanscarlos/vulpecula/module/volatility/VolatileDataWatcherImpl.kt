package top.lanscarlos.vulpecula.module.volatility

import org.bukkit.entity.Entity
import org.bukkit.entity.Pose
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.volatility
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
class VolatileDataWatcherImpl : VolatileDataWatcher {

    private val isUniversal = MinecraftVersion.isUniversal
    private val minecraftVersion = MinecraftVersion.versionId

    override fun getByteMetadata(entity: Entity, index: Int): Byte {
        val dataWatcher = (entity as Craft21Entity).handle.entityData
        return dataWatcher.get(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BYTE))
    }

    override fun getIntMetadata(entity: Entity, index: Int): Int {
        val dataWatcher = (entity as Craft21Entity).handle.entityData
        return dataWatcher.get(NMSDataWatcherObject(index, NMSDataWatcherRegistry.INT))
    }

    override fun getFloatMetadata(entity: Entity, index: Int): Float {
        val dataWatcher = (entity as Craft21Entity).handle.entityData
        return dataWatcher.get(NMSDataWatcherObject(index, NMSDataWatcherRegistry.FLOAT))
    }

    override fun getStringMetadata(entity: Entity, index: Int): Float {
        val dataWatcher = (entity as Craft21Entity).handle.entityData
        return dataWatcher.get(NMSDataWatcherObject(index, NMSDataWatcherRegistry.FLOAT))
    }

    override fun createByteMetadata(index: Int, value: Byte): Any {
        return if (minecraftVersion >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BYTE), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.a), value)
        }
    }

    override fun createIntMetadata(index: Int, value: Int): Any {
        return if (minecraftVersion >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.INT), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.b), value)
        }
    }

    override fun createFloatMetadata(index: Int, value: Float): Any {
        return if (minecraftVersion >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.FLOAT), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.c), value)
        }
    }

    override fun createStringMetadata(index: Int, value: String): Any {
        return if (minecraftVersion >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.STRING), value)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.d), value)
        }
    }

    override fun createPoseMetadata(index: Int, value: Pose): Any {
        return if (minecraftVersion >= 11900) {
            NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.POSE), NMSEntityPose.entries.find { it.name == value.name }!!)
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.s), NMS16EntityPose.entries.find { it.name == value.name }!!)
        }
    }

    override fun deconstruct(source: Pair<Int, Any>): Any {
        return when (val value = source.second) {
            is Byte -> createByteMetadata(source.first, value)
            is Int -> createIntMetadata(source.first, value)
            is Float -> createFloatMetadata(source.first, value)
            is String -> createStringMetadata(source.first, value)
            is Pose -> createPoseMetadata(source.first, value)
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