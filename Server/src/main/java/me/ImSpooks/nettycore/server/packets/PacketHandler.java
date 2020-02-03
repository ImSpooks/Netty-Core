package me.ImSpooks.nettycore.server.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.PacketType;
import me.ImSpooks.nettycore.server.packets.handle.NetworkPacketHandler;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Nick on 02 okt. 2019.
 * Copyright © ImSpooks
 */
public class PacketHandler {

    // Variables
    private final ServerPacketHandler client;
    private final ServerSettings settings;
    private final String ip;
    private final String password;

    private final AtomicBoolean connectionConfirmed = new AtomicBoolean(false);

    /**
     * Packet handler instance for connected client
     *
     * @param serverPacketHandler Server packet handler
     * @param ip Client ip
     * @param settings Server settings
     */
    public PacketHandler(ServerPacketHandler serverPacketHandler, String ip, ServerSettings settings) {
        this.client = serverPacketHandler;
        this.settings = settings;
        this.ip = ip;
        this.password = settings.getPassword();

        this.packetHandlers.put(PacketType.NETWORKING, new NetworkPacketHandler(this, settings));
    }

    private final Map<PacketType, SubPacketHandler> packetHandlers = Collections.synchronizedMap(new HashMap<>());

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PacketInRequestConnection) {
            this.packetHandlers.get(PacketType.NETWORKING).handlePacket(ctx, packet);
            return;
        }

        boolean isConfirmed;
        synchronized (this.connectionConfirmed) {
            isConfirmed = this.connectionConfirmed.get();
        }

        //only accept connection request as the first packet.
        if (!isConfirmed) {
            Logger.info("Client's first packet was not a PacketInRequestConnection but was \"" + packet.getClass().getSimpleName() + "\" instead. Closing connection...");
            ctx.close();
            return;
        }

        this.packetHandlers.get(packet.getPacketType()).handlePacket(ctx, packet);
    }

    /**
     * @return Server packet handler for client
     */
    public ServerPacketHandler getClient() {
        return client;
    }

    /**
     * @return Client's IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return Server password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Atomic boolean for confirmed connection
     */
    public AtomicBoolean getConnectionConfirmed() {
        return connectionConfirmed;
    }

    /**
     * @return Sub packet handlers for each type
     */
    public Map<PacketType, SubPacketHandler> getPacketHandlers() {
        return packetHandlers;
    }
}