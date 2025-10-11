package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.block.Block
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.common
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
@Property(bind = Block::class)
object BlockProperty : BacikalProperty<Block> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Block, key: String): Any {
        return try {
            when(key) {
                "data" -> instance.data
                "blockData" -> instance.blockData
                "type" -> instance.type
                "lightLevel" -> instance.lightLevel
                "lightFromSky" -> instance.lightFromSky
                "lightFromBlocks" -> instance.lightFromBlocks
                "world" -> instance.world
                "x" -> instance.x
                "y" -> instance.y
                "z" -> instance.z
                "location" -> instance.location
                "chunk" -> instance.chunk
                "state" -> instance.state
                "biome" -> instance.biome
                "blockPower" -> instance.blockPower
                "temperature" -> instance.temperature
                "humidity" -> instance.humidity
                "pistonMoveReaction" -> instance.pistonMoveReaction
                "drops" -> instance.drops
                "boundingBox" -> instance.boundingBox
                "collisionShape" -> instance.collisionShape
                "translationKey" -> instance.translationKey
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Block, key: String, value: Any?) {
        try {
            when(key) {
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}