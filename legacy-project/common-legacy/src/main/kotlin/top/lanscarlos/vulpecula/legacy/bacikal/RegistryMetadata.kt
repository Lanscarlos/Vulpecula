package top.lanscarlos.vulpecula.legacy.bacikal

import taboolib.library.kether.QuestActionParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-26 14:06
 */
class RegistryMetadata(
    val id: String,
    val parser: QuestActionParser,
    val aliases: Array<String>,
    val namespace: Array<String>,
    val shared: Boolean,
    val injectDefaultNamespace: Boolean
)