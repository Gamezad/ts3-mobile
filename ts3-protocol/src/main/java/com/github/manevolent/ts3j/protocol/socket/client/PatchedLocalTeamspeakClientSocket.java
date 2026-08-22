package com.github.manevolent.ts3j.protocol.socket.client;

import com.github.manevolent.ts3j.protocol.client.ClientConnectionState;
import com.github.manevolent.ts3j.protocol.packet.handler.PacketHandler;
import com.github.manevolent.ts3j.protocol.packet.handler.client.LocalClientHandlerConnected;
import com.github.manevolent.ts3j.protocol.packet.handler.client.LocalClientHandlerDisconnected;
import com.github.manevolent.ts3j.protocol.packet.handler.client.LocalClientHandlerRetrievingData;
import com.github.manevolent.ts3j.protocol.packet.handler.client.PatchedLocalClientHandlerConnecting;

/**
 * {@link LocalTeamspeakClientSocket} that swaps in
 * {@link PatchedLocalClientHandlerConnecting} for the connecting state, fixing
 * the low-security-level RSA {@code System.arraycopy} crash while leaving every
 * other state handler unchanged.
 */
public class PatchedLocalTeamspeakClientSocket extends LocalTeamspeakClientSocket {
    @Override
    protected Class<? extends PacketHandler> getHandlerClass(ClientConnectionState state) {
        switch (state) {
            case DISCONNECTED:
                return LocalClientHandlerDisconnected.class;
            case CONNECTED:
                return LocalClientHandlerConnected.class;
            case RETRIEVING_DATA:
                return LocalClientHandlerRetrievingData.class;
            case CONNECTING:
                return PatchedLocalClientHandlerConnecting.class;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
