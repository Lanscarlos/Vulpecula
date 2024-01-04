package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2024-01-03 09:55
 */
class DefaultStringReader(service: QuestService<*>, reader: BlockReader, namespace: MutableList<String>) : SimpleReader(service, reader, namespace) {

    /**
     * 行号
     * */
    var line: Int = 0

    /**
     * 跳过空白字符以及注释
     * */
    override fun skipBlank() {
        while (this.index < this.content.size) {

            when {
                this.content[this.index] == '\n' -> {
                    // 换行
                    this.index++
                    this.line++
                }
                this.content[this.index].isWhitespace() -> {
                    // 非换行空白字符
                    this.index++
                }
                this.content[this.index] == '/' && this.index + 1 < this.content.size && this.content[this.index + 1] == '/' -> {
                    // 单行注释
                    while (this.index < this.content.size && this.content[this.index] != '\n') {
                        this.index++
                    }
                }
                this.content[this.index] == '/' && this.index + 1 < this.content.size && this.content[this.index + 1] == '*' -> {
                    // 多行注释
                    while (this.index + 1 < this.content.size && (this.content[this.index] != '*' && this.content[this.index + 1] != '/')) {
                        if (this.content[this.index] == '\n') {
                            this.line++
                        }
                        this.index++
                    }

                    if (this.index + 1 < this.content.size) {
                        this.index += 2 // 确保跳过结束的多行注释符号
                    } else {
                        this.index++ // 防止越界，如果没有闭合的多行注释符号
                    }
                }
                else -> break
            }
        }
    }

    override fun nextTokenBlock(): TokenBlock {
        this.skipBlank()
        var cnt: Int

        when (peek()) {
            '"' -> {
                // 字符串
                cnt = 0
                while(this.peek() == '"') {
                    cnt++
                    this.index++
                }

                val startLine = this.line // 文本起始行号

                var met = 0
                var i = this.index
                while(i < this.content.size) {
                    if (this.content[i] == '"') {
                        met++
                    } else {
                        if (this.content[i] == '\n') {
                            this.line++
                        }
                        if (met >= cnt) {
                            break
                        }
                        met = 0
                    }
                    i++
                }

                if (met < cnt) {
                    throw LoadError.STRING_NOT_CLOSE.create(arrayOf(startLine))
                } else {
                    val ret = String(this.content, this.index, i - cnt - this.index)
                    this.index = i
                    return TokenBlock(ret, true)
                }
            }
            '\'' -> {
                this.index++
                cnt = this.index
                while (this.index < this.content.size && this.content[this.index] != '\'') {
                    this.index++
                }

                val ret = String(this.content, cnt, this.index - cnt)
                this.index++
                return TokenBlock(ret, true)
            }
            else -> {
                return TokenBlock(this.nextToken(), false)
            }
        }
    }

    override fun nextToken(): String {
        return super.nextToken()
    }

}