package io.github.ts3mobile.app.storage

data class Bookmark(
    val id: String,
    val label: String,
    val host: String,
    val port: Int = 9987,
    val nickname: String = "",
    val password: String = "",
    val defaultChannel: String = "",
) {
    val displayHost: String
        get() = if (port == 9987) host else "$host:$port"
}
