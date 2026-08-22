package io.github.ts3mobile.protocol

internal object ChannelName {
    // Spacer/alignment control tokens used by the official TeamSpeak client at
    // the start of a channel name. We remove the token but keep any label.
    private val CONTROL_PREFIX = Regex(
        """^\s*\[(?:\*?c?spacer[0-9]*|[clr]\#[0-9a-fA-F]{6}|b|i|u)\]""",
    )

    fun display(raw: String): String {
        var name = raw.replace("\r", " ").replace("\n", " ")
        // Strip only the leading control token, not arbitrary BBCode (which can be
        // part of a real styled name).
        name = CONTROL_PREFIX.replaceFirst(name, "")
        return name.trim()
    }

    fun isSpacer(raw: String): Boolean =
        Regex("""^\s*\[\*?c?spacer[0-9]*\]""", RegexOption.IGNORE_CASE).containsMatchIn(raw)
}
