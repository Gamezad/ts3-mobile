package io.github.ts3mobile.app.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom

private val Context.coldDataStore by preferencesDataStore(name = "cold_settings")

class ColdSettingsStorage(private val context: Context) {
    private val bookmarkKey = stringPreferencesKey("bookmarks")
    private val nicknameKey = stringPreferencesKey("default_nickname")
    private val random = SecureRandom()

    val bookmarks: Flow<List<Bookmark>> = context.coldDataStore.data.map { prefs ->
        decodeBookmarks(prefs[bookmarkKey].orEmpty())
    }

    val defaultNickname: Flow<String> = context.coldDataStore.data.map { prefs ->
        prefs[nicknameKey].orEmpty()
    }

    suspend fun setDefaultNickname(nickname: String) {
        context.coldDataStore.edit { prefs ->
            if (nickname.isBlank()) prefs.remove(nicknameKey)
            else prefs[nicknameKey] = nickname
        }
    }

    suspend fun addBookmark(
        label: String,
        host: String,
        port: Int,
        nickname: String,
        password: String,
        defaultChannel: String,
    ): Bookmark {
        val bookmark = Bookmark(
            id = System.currentTimeMillis().toString(36) +
                random.nextInt(0xFFFF).toString(16),
            label = label.ifBlank { host },
            host = host.trim(),
            port = port,
            nickname = nickname.trim(),
            password = password,
            defaultChannel = defaultChannel.trim(),
        )
        context.coldDataStore.edit { prefs ->
            val existing = decodeBookmarks(prefs[bookmarkKey].orEmpty()).toMutableList()
            existing.add(0, bookmark)
            prefs[bookmarkKey] = encodeBookmarks(existing)
        }
        return bookmark
    }

    suspend fun removeBookmark(id: String) {
        context.coldDataStore.edit { prefs ->
            val existing = decodeBookmarks(prefs[bookmarkKey].orEmpty())
            prefs[bookmarkKey] = encodeBookmarks(existing.filterNot { it.id == id })
        }
    }

    private fun encodeBookmarks(bookmarks: List<Bookmark>): String =
        bookmarks.joinToString(SEPARATOR) { b ->
            listOf(
                b.id,
                b.label.encode(),
                b.host.encode(),
                b.port.toString(),
                b.nickname.encode(),
                b.password.encode(),
                b.defaultChannel.encode(),
            ).joinToString(FIELD)
        }

    private fun decodeBookmarks(raw: String): List<Bookmark> {
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR).mapNotNull { line ->
            val parts = line.split(FIELD)
            if (parts.size < 7) return@mapNotNull null
            Bookmark(
                id = parts[0],
                label = parts[1].decode(),
                host = parts[2].decode(),
                port = parts[3].toIntOrNull() ?: 9987,
                nickname = parts[4].decode(),
                password = parts[5].decode(),
                defaultChannel = parts[6].decode(),
            )
        }
    }

    // Simple base64-safe encoding so fields can contain any character, including
    // pipes used as separators.
    private fun String.encode(): String =
        android.util.Base64.encodeToString(
            toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_PADDING or android.util.Base64.URL_SAFE,
        )

    private fun String.decode(): String =
        runCatching {
            String(
                android.util.Base64.decode(this, android.util.Base64.URL_SAFE),
                Charsets.UTF_8,
            )
        }.getOrDefault("")

    private companion object {
        const val FIELD = "|"
        const val SEPARATOR = "\n"
    }
}
