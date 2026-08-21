package io.github.ts3mobile.protocol

import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.net.InetSocketAddress

/**
 * Best-effort TeamSpeak SRV lookup compatible with the dnsjava version
 * pulled in by ts3j (2.1.x). Returns the highest-priority SRV target or
 * null, so the caller can safely fall back to host:9987.
 */
internal object Ts3SrvLookup {
    private const val TS3_VOICE_DEFAULT_PORT = 9987

    fun resolve(host: String, port: Int): InetSocketAddress {
        if (port != TS3_VOICE_DEFAULT_PORT) {
            return InetSocketAddress(host, port)
        }
        resolveSrv(host)?.let { return it }
        return InetSocketAddress(host, TS3_VOICE_DEFAULT_PORT)
    }

    private fun resolveSrv(host: String): InetSocketAddress? {
        val answers = try {
            val lookup = Lookup("_ts3._udp.$host", Type.SRV)
            val result = lookup.run()
            if (lookup.result != Lookup.SUCCESSFUL) null else result
        } catch (_: Exception) {
            null
        } ?: return null

        return answers.asSequence()
            .filterIsInstance<SRVRecord>()
            .sortedWith(compareBy<SRVRecord> { it.priority }.thenBy { it.weight })
            .mapNotNull { record ->
                val target = record.target?.toString()?.removeSuffix(".")?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val targetPort = record.port.takeIf { it > 0 } ?: TS3_VOICE_DEFAULT_PORT
                runCatching { InetSocketAddress(target, targetPort) }.getOrNull()
            }
            .firstOrNull { !it.isUnresolved }
    }
}
