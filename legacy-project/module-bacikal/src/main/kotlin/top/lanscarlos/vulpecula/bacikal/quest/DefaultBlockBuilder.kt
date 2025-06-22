package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-25 01:07
 */
class DefaultBlockBuilder(override var name: String) : BacikalBlockBuilder {

    constructor(key: String, content: Any): this(key) {
        when (content) {
            is String -> {
                appendContent(content)
            }
            is ConfigurationSection -> {
                val arguments = when (val value = content["arguments"]) {
                    is String -> listOf(value)
                    is List<*> -> value.mapNotNull { it?.toString() }
                    is Array<*> -> value.mapNotNull { it?.toString() }
                    else -> emptyList()
                }

                // 参数转化
                for ((i, arg) in arguments.withIndex()) {
                    appendLiteral("set $arg to &arg$i\n")
                }
                appendContent(content["content"])
            }
        }
    }

    override val namespace = mutableListOf<String>()

    override val preprocessor = StringBuilder()

    override val postprocessor = StringBuilder()

    override val content = StringBuilder()

    /**
     * 源码缓存
     * */
    val source = StringBuilder()

    override fun build(): String {
        // 清空原有源码
        val builder = source.clear()

        if (content.startsWith("def ")) {
            // 自行定义函数
            builder.append(content.toString())
            return builder.toString()
        }

        /*
        * 导入预设命名空间
        * */
        if (namespace.isNotEmpty()) {
            for (it in namespace) {
                builder.append("import $it\n")
            }
            builder.append('\n')
        }

        /*
        * 构建前置处理语句
        * */
        if (preprocessor.isNotEmpty()) {
            builder.append(preprocessor.toString())
            builder.append('\n')
        }

        /*
        * 构建核心语句
        * */
        if (content.isNotEmpty()) {
            builder.append(content.toString())
        }

        /*
        * 构建后置语句
        * */
        if (postprocessor.isNotEmpty()) {
            builder.append('\n')
            builder.append(postprocessor.toString())
        }

        /*
        * 构建方法体
        * def $name = {
        *   ...$content
        * }
        * */
        if (!builder.startsWith("def")) {
            // 提取先前所有内容
            val content = builder.extract()

            builder.append("def $name = {\n")
            builder.appendWithIndent(content)
            builder.append("}")
        }

        return builder.toString()
    }

    override fun appendPreprocessor(value: Any?) {
        if (value == null) return
        preprocessor.appendSection(value)
    }

    override fun appendPostprocessor(value: Any?) {
        if (value == null) return
        postprocessor.appendSection(value)
    }

    /**
     * 追加内容
     * */
    override fun appendContent(value: Any?) {
        if (value == null) return
        content.appendSection(value)
    }

    /**
     * 追加文本
     * */
    override fun appendLiteral(value: String) {
        content.append(value)
    }

    /**
     * 追加内容
     * */
    fun StringBuilder.appendSection(section: Any) {
        when (section) {
            is String, is StringBuilder, is StringBuffer -> {
                if (this.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    this.append('\n')
                }
                this.append(section.toString())
            }
            is Map<*, *>,
            is ConfigurationSection -> {
                val it = if (section is ConfigurationSection) {
                    section.toMap()
                } else section as Map<*, *>

                val content = it["content"]?.format(it["type"]?.toString() ?: "ke")

                if (this.isNotEmpty()) {
                    // 此前含有其他内容，换行隔开
                    this.append('\n')
                }
                this.append(content)
            }
            is List<*> -> {
                section.forEach {
                    if (it == null) return@forEach
                    appendSection(it)
                }
            }
            else -> {
                error("Unsupported section type: ${section::class.java.name} [ERROR@BacikalScriptBuilder#appendSection]")
            }
        }
    }

    /**
     * 抽取所有字符并清空容器
     * */
    fun StringBuilder.extract(): String {
        val content = this.toString()
        this.clear()
        return content
    }

    /**
     * 追加缩进内容
     * */
    fun StringBuilder.appendWithIndent(content: String) {
        content.split('\n').joinTo(
            buffer = this,
            separator = "\n    ",
            prefix = "    ",
            postfix = "\n"
        )
    }

    /**
     * 根据指定的算法格式化内容
     * */
    fun Any.format(algorithm: String = "ke"): String {
        return when (algorithm.lowercase()) {
            "ke", "kether" -> this.toString()
            "js", "javascript" -> "js '$this'"
            "ks", "script" -> "vul script run *\"$this\""
//            "kf", "fragment" -> ScriptFragment.link(this, content)
//            "kf", "fragment" -> "fragment $content"
            else -> error("Unknown format algorithm: $algorithm")
        }
    }
}