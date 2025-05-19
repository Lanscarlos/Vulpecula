package top.lanscarlos.vulpecula.module.bacikal.quest

import taboolib.common.env.RuntimeDependency

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-18 11:17
 */
@RuntimeDependency(
    value = "!io.foldright:cffu:1.1.3",
    test = "!io.foldright.cffu.CompletableFutureUtils",
    relocate = [ "!io.foldright.cffu.", "!io.foldright.cffu113." ],
)
object CffuDependency