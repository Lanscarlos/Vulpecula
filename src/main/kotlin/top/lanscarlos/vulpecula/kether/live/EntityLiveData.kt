package top.lanscarlos.vulpecula.kether.live

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecula.utils.nextBlock
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.live
 *
 * @author Lanscarlos
 * @since 2022-11-13 10:26
 */
class EntityLiveData(
    val value: Any
) : LiveData<Entity> {

    override fun get(frame: ScriptFrame, def: Entity): CompletableFuture<Entity> {
        return getOrNull(frame).thenApply { it ?: def }
    }

    override fun getOrNull(frame: ScriptFrame): CompletableFuture<Entity?> {
        val future = if (value is ParsedAction<*>) {
            frame.run(value)
        } else CompletableFuture.completedFuture(value)

        return future.thenApply {
            when (it) {
                is Entity -> it
                is ProxyPlayer -> (it as? BukkitPlayer)?.player
                is String -> Bukkit.getPlayerExact(it)
                else -> null
            }
        }
    }
}