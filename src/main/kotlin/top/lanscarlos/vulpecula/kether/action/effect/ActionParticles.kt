package top.lanscarlos.vulpecula.kether.action.effect

import taboolib.common.platform.ProxyParticle
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-10-31 16:12
 */
open class ActionParticles : ScriptAction<Any?>() {
    var particle: Any = ProxyParticle.FLAME
    var count: Any = 10
    var speed: Any = 0.0
    var offset: Any = Vector(0, 0, 0)
    var vector: Any = Vector(0, 0, 0)
    val data = HashMap<String, Any>()
    var shape: EffectShapeReader? = null

    var loc: Any? = null
    var viewer: Any? = null

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val player = frame.player()
        val loc = frame.coerceLocation(this.loc, player.location)
        val spawner = VulParticleSpawner.build(frame, setOf(player), particle, count, speed, offset, vector, data)
        this.shape?.buildEffect(frame, spawner, loc)?.show() ?: spawner.spawn(loc)
        return CompletableFuture.completedFuture(null)
    }

    fun readParticle(arg: String, reader: QuestReader): Boolean {
        when (arg) {
            "type", "t" -> {
                particle = if (reader.hasNextToken("to")) {
                    reader.nextBlock()
                } else {
                    ProxyParticle.valueOf(reader.nextToken().uppercase())
                }
            }
            "count", "c" -> {
                count = reader.readInt("to")
            }
            "speed", "sp" -> {
                speed = reader.readDouble("to")
            }
            "offset", "o" -> {
                offset = reader.readVector()
            }
            "spread", "s" -> {
                vector = reader.readVector()
            }
            "velocity", "vel", "v" -> {
                vector = reader.readVector()
                count = 0
                speed = speed.toDouble(0.0).coerceAtLeast(0.15)
            }
            "size" -> {
                data["size"] = reader.readInt("to")
            }
            "color" -> {
                data["color"] = reader.readColor()
                if (reader.hasNextToken("to")) {
                    data["transition"] = reader.readColor()
                }
            }
            "block" -> {
                data["block-mat"] = reader.readString("to")
                if (reader.hasNextToken("with")) {
                    data["meta"] = reader.readInt("to")
                }
            }
            else -> return false
        }
        return true
    }

    fun read(arg: String, reader: QuestReader): Boolean {
        if (readParticle(arg, reader)) return true
        when (arg) {
            "location", "loc" -> {
                loc = reader.nextBlock()
            }
            "viewer", "view" -> {
                viewer = reader.nextBlock()
            }
            else -> return false
        }
        return true
    }

    companion object {

        private val cache = HashMap<String, Class<out EffectShapeReader>>().also {
            it["arc"] = ShapeArc::class.java
            it["circle"] = ShapeCircle::class.java
        }

        @VulKetherParser(
            id = "particles",
            name = ["particles"]
        )
        fun parser() = scriptParser { reader ->
            reader.mark()
            when (val next = reader.nextToken().lowercase()) {
                "play", "point" -> {
                    val particles = ActionParticles()

                    if (reader.hasNextToken("at")) {
                        particles.loc = reader.nextBlock()
                    }

                    while (reader.nextPeek().startsWith('-')) {
                        reader.mark()
                        if (!particles.read(reader.nextToken().substring(1), reader)) {
                            reader.reset()
                            break
                        }
                    }

                    particles
                }
                "canvas" -> ActionParticles()
                "dynamic" -> ActionParticles()
                else -> {
                    val shape = cache[next]?.getDeclaredConstructor()?.newInstance() ?: error("Unknown argument: \"$next\"")
                    val particles = ActionParticles()
                    particles.shape = shape

                    if (reader.hasNextToken("at")) {
                        particles.loc = reader.nextBlock()
                    }

                    while (reader.nextPeek().startsWith('-')) {
                        reader.mark()
                        val arg = reader.nextToken().substring(1)

                        if (!shape.read(arg, reader)) {
                            if (!particles.read(arg, reader)) {
                                reader.reset()
                                break
                            }
                        }
                    }

                    particles
                }
            }
        }
    }
}