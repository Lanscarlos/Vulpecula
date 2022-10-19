package top.lanscarlos.vulpecula.kether

import taboolib.module.kether.ScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-19 12:30
 */
abstract class VulScriptProperty<T>(
    id: String
) : ScriptProperty<T>("vulpecula.$id.operator")