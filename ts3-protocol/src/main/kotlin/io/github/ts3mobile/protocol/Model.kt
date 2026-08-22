package io.github.ts3mobile.protocol

data class ServerConfig(
    val host: String,
    val port: Int = 9987,
    val nickname: String,
    val password: String = "",
    val defaultChannel: String = "",
) {
    /**
     * Host input may contain a port ("voice.example.com:9988") or a TeamSpeak
     * TSDNS/SRV hostname without one. When an explicit port is present in the
     * host field it overrides [port], matching how users paste addresses.
     */
    fun normalized(): ServerConfig {
        val rawHost = host.trim()
        val hostOnly: String
        val portFromHost: Int?
        if (rawHost.startsWith("[")) {
            val end = rawHost.indexOf(']')
            if (end > 1 && ':' in rawHost.substring(end)) {
                hostOnly = rawHost.substring(1, end)
                portFromHost = rawHost.substringAfterLast(':').toIntOrNull()
            } else {
                hostOnly = rawHost
                portFromHost = null
            }
        } else {
            // Only the last colon separates host from port for IPv4/hostnames.
            val colon = rawHost.lastIndexOf(':')
            if (colon > 0 && !rawHost.substring(colon + 1).contains('/')) {
                hostOnly = rawHost.substring(0, colon)
                portFromHost = rawHost.substring(colon + 1).toIntOrNull()
            } else {
                hostOnly = rawHost
                portFromHost = null
            }
        }
        return copy(
            host = hostOnly,
            port = portFromHost ?: port,
            nickname = nickname.trim(),
            defaultChannel = defaultChannel.trim(),
        )
    }

    fun validationError(): String? = when {
        host.trim().isEmpty() -> "Server address is required"
        port !in 1..65535 -> "Port must be between 1 and 65535"
        nickname.trim().length !in 2..30 -> "Nickname must contain 2 to 30 characters"
        else -> null
    }
}

enum class ConnectionPhase {
    DISCONNECTED,
    CONNECTING,
    RECONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR,
}

data class ConnectionStatus(
    val phase: ConnectionPhase = ConnectionPhase.DISCONNECTED,
    val detail: String? = null,
    val retryable: Boolean = false,
)

data class Ts3Channel(
    val id: Int,
    val parentId: Int,
    val orderAfterId: Int,
    val name: String,
    val clientCount: Int,
    val hasPassword: Boolean,
    val isDefault: Boolean,
)

data class Ts3Participant(
    val id: Int,
    val channelId: Int,
    val nickname: String,
    val isTalking: Boolean,
    val isInputMuted: Boolean,
    val isOutputMuted: Boolean,
    val uniqueIdentifier: String = "",
)

data class SessionSnapshot(
    val channels: List<Ts3Channel> = emptyList(),
    val participants: List<Ts3Participant> = emptyList(),
    val ownClientId: Int? = null,
) {
    val currentChannelId: Int?
        get() = participants.firstOrNull { it.id == ownClientId }?.channelId

    companion object {
        val Empty = SessionSnapshot()
    }
}

data class ChannelRow(
    val channel: Ts3Channel,
    val depth: Int,
)
