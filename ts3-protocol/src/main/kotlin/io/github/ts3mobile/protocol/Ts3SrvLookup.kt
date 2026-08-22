package io.github.ts3mobile.protocol

import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

internal object Ts3SrvLookup {
    private const val TS3_VOICE_DEFAULT_PORT = 9987
    private const val TSDNS_PORT = 41144

    fun resolve(host: String, port: Int): InetSocketAddress {
        if (port != TS3_VOICE_DEFAULT_PORT) return InetSocketAddress(host, port)
        resolveSrv(host)?.let { return it }
        direct(host)?.let { return it }
        resolveTsDns(host)?.let { return it }
        // TSDNS is commonly hosted on the parent domain but answers for the
        // full hostname (e.g. ts.example.com -> example.com:41144).
        parentDomain(host)?.let { parent ->
            resolveTsDns(parent, query = host)?.let { return it }
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

    private fun resolveTsDns(tsdnsHost: String, query: String = tsdnsHost): InetSocketAddress? {
        var answer: InetSocketAddress? = null
        val t = thread(name = "tsdns", isDaemon = true) {
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(tsdnsHost, TSDNS_PORT), 2_000)
                    socket.soTimeout = 2_000
                    socket.getOutputStream().bufferedWriter().use { out ->
                        out.write(query)
                        out.write("\r\n")
                        out.flush()
                    }
                    val line = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
                        ?: return@runCatching
                    val response = line.trim()
                    if (response.isBlank() || response.startsWith("4") || response.startsWith("3")) {
                        return@runCatching
                    }
                    val parts = response.split(":")
                    val resolvedHost = parts[0]
                    val resolvedPort = parts.getOrNull(1)?.toIntOrNull() ?: TS3_VOICE_DEFAULT_PORT
                    answer = InetSocketAddress(resolvedHost, resolvedPort)
                }
            }
        }
        t.join(2_500)
        if (t.isAlive) t.interrupt()
        return answer?.takeIf { !it.isUnresolved }
    }

    private fun parentDomain(host: String): String? {
        val parts = host.split('.').filter { it.isNotBlank() }
        if (parts.size < 2) return null
        return parts.drop(1).joinToString(".")
    }
}
