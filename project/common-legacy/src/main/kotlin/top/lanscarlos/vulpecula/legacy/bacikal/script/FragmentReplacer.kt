package top.lanscarlos.vulpecula.legacy.bacikal.script

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.script
 *
 * @author Lanscarlos
 * @since 2023-05-09 12:38
 */
class FragmentReplacer : ScriptTransfer {

    /**
     * 碎片
     * */
    val fragments = mutableMapOf<String, String>()

    /**
     * 是否启用全局碎片替换
     * */
    var enableGlobalFragmentReplace = true

    /**
     * 替换碎片
     * */
    override fun transfer(source: StringBuilder) {}
}