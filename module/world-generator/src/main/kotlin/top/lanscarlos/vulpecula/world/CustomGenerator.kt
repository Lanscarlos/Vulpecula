package top.lanscarlos.vulpecula.world

import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import taboolib.common.platform.function.info
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.world
 *
 * @author Lanscarlos
 * @since 2024-12-12 11:24
 */
class CustomGenerator : ChunkGenerator() {

    override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        info("calling generateNoise >> ChunkX: $chunkX, ChunkZ: $chunkZ")
    }

}