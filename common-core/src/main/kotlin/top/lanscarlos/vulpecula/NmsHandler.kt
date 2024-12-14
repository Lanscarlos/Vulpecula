package top.lanscarlos.vulpecula

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityInLevelCallback
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2024-11-28 15:58
 */
interface NmsHandler {

    fun handleEntityRemove(entity: org.bukkit.entity.Entity)

    companion object: NmsHandler by nmsProxy()

}

class NmsHandlerImpl : NmsHandler {

    override fun handleEntityRemove(entity: org.bukkit.entity.Entity) {
        val nms = (entity as CraftEntity).handle
        val property = nms.getProperty<EntityInLevelCallback?>("levelCallback", findToParent = true, remap = true)!!
        nms.setLevelCallback(property)
        info("handleEntityRemove >> EntityInLevelCallback proxy created. override.")

//        submit {
//            val property = nms.getProperty<EntityInLevelCallback?>("levelCallback", findToParent = true, remap = true)
//            if (property == null) {
//                info("handleEntityRemove >> EntityInLevelCallback is null")
//                return@submit
//            }
//            val proxy = object : EntityInLevelCallback {
//
//                override fun a() {
//                    property.a()
//                }
//
//                override fun a(p0: Entity.RemovalReason?) {
//                    info("handleEntityRemove >> onRemove by ${p0?.name}")
//                    property.a(p0)
//                }
//
//                override fun onMove() {
//                    property.onMove()
//                }
//
//                override fun onRemove(p0: Entity.RemovalReason?) {
//                    info("handleEntityRemove >> onRemove by ${p0?.name}")
//                    property.onRemove(p0)
//                }
//            }
//            nms.setProperty("levelCallback", proxy, findToParent = true, remap = true)
//            info("handleEntityRemove >> EntityInLevelCallback proxy created. override.")
//        }
//        entity.remove()
//        nms.invokeLocalMethod<Any?>("remove", Entity.RemovalReason.a)
//        nms.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED)
//        nms.setProperty("levelCallback", proxy, findToParent = true, remap = true)
    }
}