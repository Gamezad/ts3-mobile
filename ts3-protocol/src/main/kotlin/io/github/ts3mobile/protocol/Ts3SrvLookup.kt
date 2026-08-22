package io.github.ts3mobile.protocol

import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.net.InetSocketAddress

internal object Ts3SrvLookup {
    private const val TS3_VOICE_DEFAULT_PORT = 9987

    fun resolve(host: String, port: Int): InetSocketAddress {
        if (port != TS3_VOICE_DEFAULT_PORT) return InetSocketAddress(host, port)
        resolveSrv(host)?.let { return it }
        direct(host)?.let { return it }
        parentDomain(host)?.let { parent ->
            resolveSrv(parent)?.let { return it }
            direct(parent)?.let { return it }
        }
        return InetSocketAddress(host, TS3_VOICE_DEFAULT_PORT)
    }

    private fun direct(host: String): InetSocketAddress? =
        runCatching { InetSocketAddress(host, TS3_VOICE_DEFAULT_PORT) }
            .getOrNull()?.takeIf { !it.isUnresolved }

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
                val target = record.target?.toString()?.removeSuffix(".")
                    ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val targetPort = record.port.takeIf { it > 0 } ?: TS3_VOICE_DEFAULT_PORT
                runCatching { InetSocketAddress(target, targetPort) }.getOrNull()
            }
            .firstOrNull { !it.isUnresolved }
    }

    private fun parentDomain(host: String): String? {
        val parts = host.split('.').filter { it.isNotBlank() }
        if (parts.size < 2) return null
        return parts.drop(1).joinToString(".")
    }
}
