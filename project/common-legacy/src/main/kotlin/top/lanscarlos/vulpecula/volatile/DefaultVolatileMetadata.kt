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
    private val majorLegacy = MinecraftVersion.majorLegacy

    override fun createByteMetadata(index: Int, value: Byte): Any {
        return if (isUniversal) {
            val metadata = NMSDataWatcherItem(NMSDataWatcherObject(index, NMSDataWatcherRegistry.BYTE), value)
            if (majorLegacy >= 11903) {
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
            if (majorLegacy >= 11903) {
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
            if (majorLegacy >= 11903) {
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
            if (majorLegacy >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.d), value)
        }
    }

    override fun createPoseMetadata(index: Int, value: VolatilePose): Any {
        return if (majorLegacy >= 11900) {
            val metadata = NMSDataWatcherItem(
                NMSDataWatcherObject(index, NMSDataWatcherRegistry.POSE),
                when (value) {
                    VolatilePose.STANDING -> NMSEntityPose.STANDING
                    VolatilePose.FALL_FLYING -> NMSEntityPose.FALL_FLYING
                    VolatilePose.SLEEPING -> NMSEntityPose.SLEEPING
                    VolatilePose.SWIMMING -> NMSEntityPose.SWIMMING
                    VolatilePose.SPIN_ATTACK -> NMSEntityPose.SPIN_ATTACK
                    VolatilePose.CROUCHING -> NMSEntityPose.CROUCHING
                    VolatilePose.DYING -> NMSEntityPose.DYING
                }
            )

            if (majorLegacy >= 11903) {
                metadata.value()
            } else {
                metadata
            }
        } else {
            NMS16DataWatcherItem(
                NMS16DataWatcherObject(index, NMS16DataWatcherRegistry.s),
                when (value) {
                    VolatilePose.STANDING -> NMS16EntityPose.STANDING
                    VolatilePose.FALL_FLYING -> NMS16EntityPose.FALL_FLYING
                    VolatilePose.SLEEPING -> NMS16EntityPose.SLEEPING
                    VolatilePose.SWIMMING -> NMS16EntityPose.SWIMMING
                    VolatilePose.SPIN_ATTACK -> NMS16EntityPose.SPIN_ATTACK
                    VolatilePose.CROUCHING -> NMS16EntityPose.CROUCHING
                    VolatilePose.DYING -> NMS16EntityPose.DYING
                }
            )
        }
    }

}

typealias NMSEntityPose = net.minecraft.world.entity.EntityPose
typealias NMS16EntityPose = net.minecraft.server.v1_16_R3.EntityPose

typealias NMSDataWatcherItem<T> = net.minecraft.network.syncher.DataWatcher.Item<T>
typealias NMSDataWatcherObject<T> = net.minecraft.network.syncher.DataWatcherObject<T>
typealias NMSDataWatcherRegistry = net.minecraft.network.syncher.DataWatcherRegistry

typealias NMS16DataWatcherItem<T> = net.minecraft.server.v1_16_R3.DataWatcher.Item<T>
typealias NMS16DataWatcherObject<T> = net.minecraft.server.v1_16_R3.DataWatcherObject<T>
typealias NMS16DataWatcherRegistry = net.minecraft.server.v1_16_R3.DataWatcherRegistry