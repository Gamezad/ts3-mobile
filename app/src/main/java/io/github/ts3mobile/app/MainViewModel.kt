package io.github.ts3mobile.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.ts3mobile.app.storage.Bookmark
import io.github.ts3mobile.app.storage.ColdSettingsStorage
import io.github.ts3mobile.protocol.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConnectionFormState(
    val host: String = "",
    val port: String = "9987",
    val nickname: String = "ColdTs User",
    val password: String = "",
    val defaultChannel: String = "",
    val submitted: Boolean = false,
) {
    fun toServerConfigOrNull(): ServerConfig? {
        val portValue = port.toIntOrNull() ?: 9987
        val config = ServerConfig(
            host = host,
            port = portValue,
            nickname = nickname,
            password = password,
            defaultChannel = defaultChannel,
        ).normalized()
        return config.takeIf { it.validationError() == null }
    }
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = ColdSettingsStorage(app)

    private val mutableForm = MutableStateFlow(ConnectionFormState())
    val form: StateFlow<ConnectionFormState> = mutableForm.asStateFlow()

    val bookmarks: StateFlow<List<Bookmark>> = storage.bookmarks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    init {
        viewModelScope.launch {
            storage.defaultNickname.collect { saved ->
                if (saved.isNotBlank() && mutableForm.value.nickname.isBlank()) {
                    mutableForm.update { it.copy(nickname = saved) }
                }
            }
        }
    }

    fun setHost(value: String) {
        // If the user pastes host:port, split it into the two fields.
        val trimmed = value.trim()
        val hostValue: String
        val portValue: String?
        if (trimmed.startsWith("[")) {
            val end = trimmed.indexOf(']')
            if (end > 1) {
                hostValue = trimmed.substring(0, end + 1)
                portValue = trimmed.substringAfter(']', "")
                    .removePrefix(":").ifBlank { null }
            } else {
                hostValue = value
                portValue = null
            }
        } else {
            val colon = trimmed.lastIndexOf(':')
            if (colon > 0) {
                val after = trimmed.substring(colon + 1)
                if (after.toIntOrNull() != null && after.length <= 5) {
                    hostValue = trimmed.substring(0, colon)
                    portValue = after
                } else {
                    hostValue = value
                    portValue = null
                }
            } else {
                hostValue = value
                portValue = null
            }
        }
        mutableForm.update {
            it.copy(
                host = hostValue,
                port = portValue ?: it.port,
                submitted = false,
            )
        }
    }

    fun setPort(value: String) =
        mutableForm.update { it.copy(port = value.filter(Char::isDigit).take(5), submitted = false) }

    fun setNickname(value: String) =
        mutableForm.update { it.copy(nickname = value, submitted = false) }

    fun setPassword(value: String) =
        mutableForm.update { it.copy(password = value, submitted = false) }

    fun setDefaultChannel(value: String) =
        mutableForm.update { it.copy(defaultChannel = value, submitted = false) }

    fun applyBookmark(bookmark: Bookmark) {
        mutableForm.update {
            it.copy(
                host = bookmark.host,
                port = bookmark.port.toString(),
                nickname = bookmark.nickname.ifBlank { it.nickname },
                password = bookmark.password,
                defaultChannel = bookmark.defaultChannel,
                submitted = false,
            )
        }
    }

    fun saveBookmark() {
        val current = mutableForm.value
        val host = current.host.trim()
        if (host.isEmpty()) return
        viewModelScope.launch {
            storage.addBookmark(
                label = host.substringBefore('.'),
                host = host,
                port = current.port.toIntOrNull() ?: 9987,
                nickname = current.nickname,
                password = current.password,
                defaultChannel = current.defaultChannel,
            )
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch { storage.removeBookmark(id) }
    }

    fun persistNickname() {
        val nickname = mutableForm.value.nickname.trim()
        if (nickname.length in 2..30) {
            viewModelScope.launch { storage.setDefaultNickname(nickname) }
        }
    }

    fun submit(): ServerConfig? {
        mutableForm.update { it.copy(submitted = true) }
        return mutableForm.value.toServerConfigOrNull()
    }
}
