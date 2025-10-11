package top.lanscarlos.vulpecula.module.property.common

import org.bukkit.World
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property.common
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
@Property(bind = World::class)
object WorldProperty : BacikalProperty<World> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: World, key: String): Any? {
        return try {
            when(key) {
                "loadedChunks" -> instance.loadedChunks
                "forceLoadedChunks" -> instance.forceLoadedChunks
                "pluginChunkTickets" -> instance.pluginChunkTickets
                "entities" -> instance.entities
                "livingEntities" -> instance.livingEntities
                "players" -> instance.players
                "spawnLocation" -> instance.spawnLocation
                "time" -> instance.time
                "fullTime" -> instance.fullTime
                "gameTime" -> instance.gameTime
                "weatherDuration" -> instance.weatherDuration
                "thunderDuration" -> instance.thunderDuration
                "clearWeatherDuration" -> instance.clearWeatherDuration
                "pVP" -> instance.pvp
                "generator" -> instance.generator
                "biomeProvider" -> instance.biomeProvider
                "populators" -> instance.populators
                "allowAnimals" -> instance.allowAnimals
                "allowMonsters" -> instance.allowMonsters
                "logicalHeight" -> instance.logicalHeight
                "seaLevel" -> instance.seaLevel
                "keepSpawnInMemory" -> instance.keepSpawnInMemory
                "difficulty" -> instance.difficulty
                "viewDistance" -> instance.viewDistance
                "simulationDistance" -> instance.simulationDistance
                "worldFolder" -> instance.worldFolder
                "worldType" -> instance.worldType
                "ticksPerAnimalSpawns" -> instance.ticksPerAnimalSpawns
                "ticksPerMonsterSpawns" -> instance.ticksPerMonsterSpawns
                "ticksPerWaterSpawns" -> instance.ticksPerWaterSpawns
                "ticksPerWaterAmbientSpawns" -> instance.ticksPerWaterAmbientSpawns
                "ticksPerWaterUndergroundCreatureSpawns" -> instance.ticksPerWaterUndergroundCreatureSpawns
                "ticksPerAmbientSpawns" -> instance.ticksPerAmbientSpawns
                "monsterSpawnLimit" -> instance.monsterSpawnLimit
                "animalSpawnLimit" -> instance.animalSpawnLimit
                "waterAnimalSpawnLimit" -> instance.waterAnimalSpawnLimit
                "waterUndergroundCreatureSpawnLimit" -> instance.waterUndergroundCreatureSpawnLimit
                "waterAmbientSpawnLimit" -> instance.waterAmbientSpawnLimit
                "ambientSpawnLimit" -> instance.ambientSpawnLimit
                "gameRules" -> instance.gameRules
                "worldBorder" -> instance.worldBorder
                "raids" -> instance.raids
                "enderDragonBattle" -> instance.enderDragonBattle
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: World, key: String, value: Any?) {
        try {
            when(key) {
                "spawnLocation" -> instance.spawnLocation = value.let(BukkitLocationApplicative::convert)
                "time" -> instance.time = value.let(LongApplicative::convert)
                "fullTime" -> instance.fullTime = value.let(LongApplicative::convert)
                "storm" -> instance.setStorm(value.let(BooleanApplicative::convert))
                "weatherDuration" -> instance.weatherDuration = value.let(IntApplicative::convert)
                "thundering" -> instance.isThundering = value.let(BooleanApplicative::convert)
                "thunderDuration" -> instance.thunderDuration = value.let(IntApplicative::convert)
                "clearWeatherDuration" -> instance.clearWeatherDuration = value.let(IntApplicative::convert)
                "pVP" -> instance.pvp = value.let(BooleanApplicative::convert)
                "keepSpawnInMemory" -> instance.keepSpawnInMemory = value.let(BooleanApplicative::convert)
                "autoSave" -> instance.isAutoSave = value.let(BooleanApplicative::convert)
                "hardcore" -> instance.isHardcore = value.let(BooleanApplicative::convert)
                "ticksPerAnimalSpawns" -> instance.setTicksPerAnimalSpawns(value.let(IntApplicative::convert))
                "ticksPerMonsterSpawns" -> instance.setTicksPerMonsterSpawns(value.let(IntApplicative::convert))
                "ticksPerWaterSpawns" -> instance.setTicksPerWaterSpawns(value.let(IntApplicative::convert))
                "ticksPerWaterAmbientSpawns" -> instance.setTicksPerWaterAmbientSpawns(value.let(IntApplicative::convert))
                "ticksPerWaterUndergroundCreatureSpawns" -> instance.setTicksPerWaterUndergroundCreatureSpawns(value.let(IntApplicative::convert))
                "ticksPerAmbientSpawns" -> instance.setTicksPerAmbientSpawns(value.let(IntApplicative::convert))
                "monsterSpawnLimit" -> instance.monsterSpawnLimit = value.let(IntApplicative::convert)
                "animalSpawnLimit" -> instance.animalSpawnLimit = value.let(IntApplicative::convert)
                "waterAnimalSpawnLimit" -> instance.waterAnimalSpawnLimit = value.let(IntApplicative::convert)
                "waterUndergroundCreatureSpawnLimit" -> instance.waterUndergroundCreatureSpawnLimit = value.let(IntApplicative::convert)
                "waterAmbientSpawnLimit" -> instance.waterAmbientSpawnLimit = value.let(IntApplicative::convert)
                "ambientSpawnLimit" -> instance.ambientSpawnLimit = value.let(IntApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}