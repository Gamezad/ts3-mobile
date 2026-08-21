package com.github.manevolent.ts3j.protocol.packet.handler.client;

import com.github.manevolent.ts3j.command.Command;
import com.github.manevolent.ts3j.command.SingleCommand;
import com.github.manevolent.ts3j.command.parameter.CommandSingleParameter;
import com.github.manevolent.ts3j.protocol.Packet;
import com.github.manevolent.ts3j.protocol.ProtocolRole;
import com.github.manevolent.ts3j.protocol.client.ClientConnectionState;
import com.github.manevolent.ts3j.protocol.packet.PacketBody;
import com.github.manevolent.ts3j.protocol.packet.PacketBody2Command;
import com.github.manevolent.ts3j.protocol.packet.PacketBody8Init1;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
import com.github.manevolent.ts3j.util.Ts3Crypt;
import com.github.manevolent.ts3j.util.Ts3Debugging;
import com.github.manevolent.ts3j.util.Pair;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Drop-in replacement for ts3j's {@link LocalClientHandlerConnecting} that fixes
 * {@code System.arraycopy} crashing when the server uses a low RSA security
 * level (the original code computes the source offset as
 * {@code Math.abs(solution.length - 64)} instead of zero, so short solutions
 * fail with "srcPos >= src.length").
 *
 * <p>Placed in ts3j's own package so it can call package-private helpers on
 * {@link LocalTeamspeakClientSocket} (e.g. {@code setSecureParameters}).
 */
public class PatchedLocalClientHandlerConnecting extends LocalClientHandler {
    private static final byte[] INIT1_VERSION =
            new byte[]{0x0C, (byte) 0xFF, (byte) 0xD2, (byte) 0xFE};

    private byte[] randomBytes;
    private byte[] alphaBytes;

    public PatchedLocalClientHandlerConnecting(LocalTeamspeakClientSocket client) {
        super(client);
    }

    @Override
    public void onAssigned() throws IOException, TimeoutException {
        PacketBody8Init1 packet = new PacketBody8Init1(ProtocolRole.CLIENT);

        PacketBody8Init1.Step0 step = new PacketBody8Init1.Step0();
        Random random = new Random();
        randomBytes = new byte[4];
        random.nextBytes(randomBytes);
        step.setRandom(randomBytes);
        step.setTimestamp((int) (System.currentTimeMillis() / 1000L));
        packet.setStep(step);

        sendInit1(packet);
    }

    private void sendInit1(PacketBody8Init1 packet) throws IOException, TimeoutException {
        packet.setVersion(INIT1_VERSION);
        getClient().writePacket(packet);
    }

    @Override
    public void handlePacket(Packet packet) throws IOException, TimeoutException {
        if (packet.getBody() instanceof PacketBody8Init1) {
            PacketBody8Init1 init1 = (PacketBody8Init1) packet.getBody();

            Ts3Debugging.debug("Handle Init1 step " + init1.getStep().getNumber());
            PacketBody8Init1.Step step;

            switch (init1.getStep().getNumber()) {
                case 1:
                    PacketBody8Init1.Step1 serverReplyStep1 =
                            (PacketBody8Init1.Step1) init1.getStep();

                    // Verify nonce; received in reversed order.
                    for (int i = 0; i < 4; i++) {
                        if (randomBytes[3 - i] != serverReplyStep1.getA0reversed()[i]) {
                            Ts3Debugging.debug("[WARNING] random byte mismatch!");
                            break;
                        }
                    }

                    PacketBody8Init1.Step2 step2 = new PacketBody8Init1.Step2();
                    step2.setA0reversed(serverReplyStep1.getA0reversed());
                    step2.setServerStuff(serverReplyStep1.getServerStuff());
                    step = step2;
                    break;

                case 3:
                    PacketBody8Init1.Step3 serverReplyStep3 =
                            (PacketBody8Init1.Step3) init1.getStep();

                    if (serverReplyStep3.getLevel() < 0 ||
                            serverReplyStep3.getLevel() > 1_000_000) {
                        throw new IllegalArgumentException(
                                "RSA challenge level is not within an acceptable range");
                    }

                    BigInteger x = new BigInteger(1, serverReplyStep3.getX());
                    BigInteger n = new BigInteger(1, serverReplyStep3.getN());

                    // x^(2^level) mod n, returned as an unsigned big-endian
                    // integer padded to exactly 64 bytes (left-padded with
                    // zeros). The upstream library mistakenly used
                    // Math.abs(solution.length - 64) as the source offset,
                    // which throws for solution lengths under 64 bytes.
                    byte[] solution = x.modPow(
                            BigInteger.valueOf(2L).pow(serverReplyStep3.getLevel()),
                            n
                    ).toByteArray();

                    byte[] y = new byte[64];
                    int copyLength = Math.min(solution.length, 64);
                    int srcOffset = Math.max(0, solution.length - copyLength);
                    System.arraycopy(
                            solution,
                            srcOffset,
                            y,
                            64 - copyLength,
                            copyLength
                    );

                    PacketBody8Init1.Step4 step4 = new PacketBody8Init1.Step4();

                    step4.setLevel(serverReplyStep3.getLevel());
                    step4.setX(serverReplyStep3.getX());
                    step4.setN(serverReplyStep3.getN());
                    step4.setY(y);
                    step4.setServerStuff(serverReplyStep3.getServerStuff());

                    step4.setClientIVcommand(
                            createInitIv().build().getBytes(Charset.forName("UTF8"))
                    );
                    step = step4;
                    break;

                case 127:
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    onAssigned();
                    return;

                default:
                    throw new IllegalArgumentException(
                            "unexpected Init1 server step: " + init1.getStep().getNumber());
            }

            sendInit1(new PacketBody8Init1(getClient().getRole().getOut(), step));
        } else if (packet.getBody() instanceof PacketBody2Command) {
            SingleCommand command =
                    ((PacketBody2Command) packet.getBody()).parse().simplifyOne();

            Ts3Debugging.debug(command.build());

            if (command.getName().equalsIgnoreCase("initivexpand")) {
                byte[] alpha = Base64.getDecoder().decode(command.get("alpha").getValue());
                byte[] beta = Base64.getDecoder().decode(command.get("beta").getValue());
                byte[] omega = Base64.getDecoder().decode(command.get("omega").getValue());

                getClient().setSecureParameters(
                        Ts3Crypt.cryptoInit(alpha, beta, omega, getClient().getIdentity())
                );

                sendClientInit();

                getClient().setCommandProcessor(getClient());
                getClient().setState(ClientConnectionState.RETRIEVING_DATA);
            } else if (command.getName().equalsIgnoreCase("initivexpand2")) {
                if (!command.get("ot").getValue().equals("1")) {
                    throw new IllegalArgumentException(
                            "ot constant != 1: " + command.get("ot").getValue());
                }

                byte[] license = Base64.getDecoder().decode(command.get("l").getValue());
                byte[] beta = Base64.getDecoder().decode(command.get("beta").getValue());
                byte[] omega = Base64.getDecoder().decode(command.get("omega").getValue());
                byte[] proof = Base64.getDecoder().decode(command.get("proof").getValue());

                Pair<byte[], byte[]> keyPair = Ts3Crypt.generateKeypair();

                ECPoint publicKey = Ts3Crypt.decodePublicKey(omega);

                if (!Ts3Crypt.verifySignature(publicKey, license, proof)) {
                    throw new SecurityException(
                            "invalid proof signature: " + Ts3Debugging.getHex(proof));
                }

                byte[] signature = Ts3Crypt.generateClientEkProof(
                        keyPair.getKey(),
                        beta,
                        getClient().getIdentity()
                );

                getClient().writePacket(new PacketBody2Command(
                        ProtocolRole.CLIENT,
                        new SingleCommand(
                                "clientek",
                                ProtocolRole.CLIENT,
                                new CommandSingleParameter(
                                        "ek",
                                        Base64.getEncoder().encodeToString(keyPair.getKey())
                                ),
                                new CommandSingleParameter(
                                        "proof",
                                        Base64.getEncoder().encodeToString(signature)
                                )
                        )
                ));

                getClient().setSecureParameters(
                        Ts3Crypt.cryptoInit2(license, alphaBytes, beta, keyPair.getValue())
                );

                alphaBytes = null;

                sendClientInit();

                getClient().setCommandProcessor(getClient());
                getClient().setState(ClientConnectionState.RETRIEVING_DATA);
            } else if (command.getName().equals("error")) {
                getClient().setState(ClientConnectionState.DISCONNECTED);
                throw new IOException(command.get("msg").getValue());
            } else {
                throw new IOException("Unknown Init command: " + command.getName());
            }
        }
    }

    private SingleCommand createInitIv() {
        alphaBytes = new byte[10];
        new Random().nextBytes(alphaBytes);

        SingleCommand initiv = new SingleCommand("clientinitiv", ProtocolRole.CLIENT);
        initiv.add(new CommandSingleParameter(
                "alpha",
                Base64.getEncoder().encodeToString(alphaBytes)
        ));
        initiv.add(new CommandSingleParameter(
                "omega",
                getClient().getIdentity().getPublicKeyString()
        ));
        initiv.add(new CommandSingleParameter("ot", "1"));

        InetAddress address = getClient().getRemoteSocketAddress().getAddress();
        if (address != null) {
            String hostAddress = address.getHostAddress();
            if (hostAddress != null && hostAddress.startsWith(":")) {
                hostAddress = hostAddress.substring(1);
            }
            initiv.add(new CommandSingleParameter("ip", hostAddress));
        }

        return initiv;
    }

    private void sendClientInit() throws IOException, TimeoutException {
        LocalTeamspeakClientSocket client = getClient();

        String version = client.getOption("client.version_string", String.class);
        if (version == null) version = "3.?.? [Build: 5680278000]";

        String platform = client.getOption("client.version_platform", String.class);
        if (platform == null) platform = "Windows";

        String versionSign = client.getOption("client.version_sign", String.class);
        if (versionSign == null) {
            versionSign =
                    "DX5NIYLvfJEUjuIbCidnoeozxIDRRkpq3I9vVMBmE9L2qnekOoBzSenkzsg2lC9CMv8K5hkEzhr2TYUYSwUXCg==";
        }

        String hwid = client.getOption("client.hwid", String.class);
        if (hwid == null) hwid = "+LyYqbDqOvEEpN5pdAbF8/v5kZ0=";

        Command clientinit = new SingleCommand(
                "clientinit",
                ProtocolRole.CLIENT,
                new CommandSingleParameter("client_nickname", client.getNickname()),
                new CommandSingleParameter("client_version", version),
                new CommandSingleParameter("client_platform", platform),
                new CommandSingleParameter("client_version_sign", versionSign),
                new CommandSingleParameter("client_input_hardware", "1"),
                new CommandSingleParameter("client_output_hardware", "1"),
                new CommandSingleParameter(
                        "client_default_channel",
                        client.getOption("client.default_channel", String.class)
                ),
                new CommandSingleParameter(
                        "client_default_channel_password",
                        client.getOption("client.default_channel_password", String.class)
                ),
                new CommandSingleParameter(
                        "client_server_password",
                        client.getOption("client.server_password", String.class)
                ),
                new CommandSingleParameter(
                        "client_nickname_phonetic",
                        client.getOption("client.nickname_phonetic", String.class)
                ),
                new CommandSingleParameter("client_meta_data", ""),
                new CommandSingleParameter(
                        "client_default_token",
                        client.getOption("client.default_token", String.class)
                ),
                new CommandSingleParameter(
                        "client_key_offset",
                        Long.toString(client.getIdentity().getKeyOffset())
                ),
                new CommandSingleParameter("hwid", hwid)
        );

        Ts3Debugging.debug(clientinit.build());

        client.writePacket(new PacketBody2Command(ProtocolRole.CLIENT, clientinit));
    }
}
