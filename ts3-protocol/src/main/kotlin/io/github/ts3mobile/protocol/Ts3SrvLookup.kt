package io.github.ts3mobile.protocol

import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

/**
 * TeamSpeak 3 endpoint resolution, matching official client order:
 *  1. explicit port -> direct
 *  2. SRV record `_ts3._udp.<host>`
 *  3. direct host:9987
 *  4. TSDNS query on TCP 41144 (can return "host:port" or error)
 */
internal object Ts3SrvLookup {
    private const val TS3_VOICE_DEFAULT_PORT = 9987
    private const val TSDNS_PORT = 41144

    fun resolve(host: String, port: Int): InetSocketAddress {
        if (port != TS3_VOICE_DEFAULT_PORT) return InetSocketAddress(host, port)
        resolveSrv(host)?.let { return it }
        direct(host)?.let { return it }
        resolveTsDns(host)?.let { return it }
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

    private fun resolveTsDns(host: String): InetSocketAddress? = try {
        // TSDNS is a simple line-based TCP protocol: connect to port 41144,
        // send the hostname + "\r\n", read a response line.
        // Response "host:port" or "host" (port 9987) means use that.
        // "404" or "3xx" means not found.
        lateinit var result: InetSocketAddress
        val t = thread(name = "tsdns", isDaemon = true) {
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, TSDNS_PORT), 2_000)
                    socket.soTimeout = 2_000
                    socket.getOutputStream().bufferedWriter().use { out ->
                        out.write(host); out.write("\r\n"); out.flush()
                    }
                    val line = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
                        ?: return@runCatching
                    val answer = line.trim()
                    if (answer.isBlank() || answer.startsWith("4") || answer.startsWith("3")) return@runCatching
                    val parts = answer.split(":")
                    val resolvedHost = parts[0]
                    val resolvedPort = parts.getOrNull(1)?.toIntOrNull() ?: TS3_VOICE_DEFAULT_PORT
                    result = InetSocketAddress(resolvedHost, resolvedPort)
                }
            }
        }
        t.join(2_500)
        if (t.isAlive) t.interrupt()
        if (::result.isInitialized && !result.isUnresolved) result else null
    } catch (_: Throwable) {
        null
    }
}
