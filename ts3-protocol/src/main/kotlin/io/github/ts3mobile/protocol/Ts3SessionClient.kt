package io.github.ts3mobile.protocol

interface Ts3SessionListener {
    fun onStatusChanged(status: ConnectionStatus)
    fun onSnapshotChanged(snapshot: SessionSnapshot)
    fun onVoiceFrame(frame: VoiceFrame)
}

interface Ts3SessionClient : AutoCloseable {
    fun connect(config: ServerConfig, identityMaterial: String, listener: Ts3SessionListener)
    fun setVoiceSource(source: EncodedVoiceSource?)
    fun joinChannel(channelId: Int, password: String = "")
    fun setNickname(nickname: String)
    fun setInputMuted(muted: Boolean)
    fun setOutputMuted(muted: Boolean)
    fun setAway(message: String?)
    fun sendChannelMessage(message: String)
    fun disconnect(reason: String = "Client disconnected")
    override fun close()
}
