package io.github.ts3mobile.audio.opus

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioRoutingTest {
    private val routes = listOf(
        AudioRoutingState.SystemRoute,
        AudioRouteOption(7, AudioRouteKind.SPEAKER, "Speaker"),
        AudioRouteOption(12, AudioRouteKind.BLUETOOTH, "Bluetooth device"),
    )

    @Test
    fun keepsAnAvailableSelection() {
        assertEquals(12, resolveSelectedRouteId(12, routes))
    }

    @Test
    fun fallsBackToSystemWhenADeviceDisappears() {
        assertEquals(SYSTEM_AUDIO_ROUTE_ID, resolveSelectedRouteId(99, routes))
    }
}
