package io.github.ts3mobile.protocol

/**
 * A single inbound/outbound text message, normalised across TeamSpeak's
 * channel/private/server target modes.
 */
data class ChatMessage(
    val author: String,
    val text: String,
    val target: Target,
    val timestamp: Long = System.currentTimeMillis(),
    val isOwn: Boolean = false,
) {
    enum class Target { CHANNEL, SERVER, PRIVATE }
}
