package io.github.ts3mobile.app.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "cold_app_settings")

data class AppSettings(
    val darkTheme: Boolean = false,
    val masterVolume: Float = 1f,
    val notificationSounds: Boolean = false,
    val denoiserEnabled: Boolean = true,
    val echoCancelerEnabled: Boolean = true,
    val autoGainControlEnabled: Boolean = false,
)

class AppSettingsStorage(context: Context) {
    private val store = context.appDataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            darkTheme = prefs[Keys.DARK_THEME] ?: false,
            masterVolume = prefs[Keys.MASTER_VOLUME] ?: 1f,
            notificationSounds = prefs[Keys.NOTIFICATION_SOUNDS] ?: false,
            denoiserEnabled = prefs[Keys.DENOISER] ?: true,
            echoCancelerEnabled = prefs[Keys.AEC] ?: true,
            autoGainControlEnabled = prefs[Keys.AGC] ?: false,
        )
    }

    suspend fun setDarkTheme(value: Boolean) = store.edit { it[Keys.DARK_THEME] = value }
    suspend fun setMasterVolume(value: Float) =
        store.edit { it[Keys.MASTER_VOLUME] = value.coerceIn(0f, 1f) }
    suspend fun setNotificationSounds(value: Boolean) =
        store.edit { it[Keys.NOTIFICATION_SOUNDS] = value }
    suspend fun setDenoiser(value: Boolean) = store.edit { it[Keys.DENOISER] = value }
    suspend fun setEchoCanceler(value: Boolean) = store.edit { it[Keys.AEC] = value }
    suspend fun setAutoGainControl(value: Boolean) = store.edit { it[Keys.AGC] = value }

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val MASTER_VOLUME = floatPreferencesKey("master_volume")
        val NOTIFICATION_SOUNDS = booleanPreferencesKey("notification_sounds")
        val DENOISER = booleanPreferencesKey("denoiser_enabled")
        val AEC = booleanPreferencesKey("aec_enabled")
        val AGC = booleanPreferencesKey("agc_enabled")
    }
}
