package top.lanscarlos.vulpecula.bacikal.action.illusion

import ink.ptms.adyeshach.core.Adyeshach
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submit
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.run
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.union

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.illusion
 *
 * @author Lanscarlos
 * @since 2023-08-10 12:37
 */
object ActionIllusionHologram : ActionIllusion.Resolver {

    val adyeshachHologramAPI by lazy {
        try {
            Adyeshach.api().getHologramHandler()
        } catch (ex: Exception) {
            null
        }
    }

    override val name = arrayOf("hologram")

    override fun resolve(reader: ActionIllusion.Reader): Bacikal.Parser<Any?> {
        return reader.run {
            combine(
                source(),
                LiveData {
                    val cache = mutableSetOf<ParsedAction<*>>()
                    if (this.expectToken("[")) {
                        while (!this.expectToken("]")) {
                            cache += this.readAction()
                        }
                    } else {
                        cache += this.readAction()
                    }

                    Bacikal.Action { frame ->
                        cache.map { frame.run(it) }.union()
                    }
                },
                optional("at", then = location()),
                argument("duration", "time", then = long(), def = 200)
            ) { target, content, location, duration ->
                val api = adyeshachHologramAPI ?: error("Unsupported illusion hologram. Please install Adyeshach v2.")
                val items = content.map {
                    when (it) {
                        is String -> api.createHologramItem(it)
                        is List<*> -> api.createHologramItem(it.joinToString { ", " })
                        is ItemStack -> api.createHologramItem(it)
                        else -> it.toString()
                    }
                }
                target.forEach {
                    val hologram = api.createHologram(it, location?.toBukkitLocation() ?: it.location, items, isolate = false)
                    if (duration > 0) {
                        submit(delay = duration) {
                            hologram.remove()
                        }
                    }
                }
            }
        }
    }

}