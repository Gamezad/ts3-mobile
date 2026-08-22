package io.github.ts3mobile.protocol

internal object ChannelName {
    private val SPACER = Regex("""^\[\*?c?spacer[0-9]*]\s*""", RegexOption.IGNORE_CASE)
    private val BBCODE = Regex("""\[/?[^\]]+]""")

    fun display(raw: String): String {
        var name = raw.replace("\r", " ").replace("\n", " ")
        name = SPACER.replaceFirst(name, "")
        name = BBCODE.replace(name, "")
        return name.trim()
    }

    fun isSpacer(raw: String): Boolean = SPACER.containsMatchIn(raw)
}
