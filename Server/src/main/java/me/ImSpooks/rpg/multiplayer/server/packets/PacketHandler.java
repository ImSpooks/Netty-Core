package me.ImSpooks.rpg.multiplayer.server.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import me.ImSpooks.rpg.multiplayer.packets.handle.PacketType;
import me.ImSpooks.rpg.multiplayer.server.packets.handle.NetworkPacketHandler;
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

    private final ServerPacketHandler client;
    private final String ip;
    private final String password;

    private final AtomicBoolean connectionConfirmed = new AtomicBoolean(false);

    public PacketHandler(ServerPacketHandler serverPacketHandler, String ip, String password) {
        this.client = serverPacketHandler;
        this.ip = ip;
        this.password = password;

        this.packetHandlers.put(PacketType.NETWORKING, new NetworkPacketHandler(this));
    }

    private final Map<PacketType, SubPacketHandler> packetHandlers = Collections.synchronizedMap(new HashMap<>());

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PacketInRequestConnection) {
            this.packetHandlers.get(PacketType.NETWORKING).handlePacket(ctx, (PacketInRequestConnection) packet);
            return;
        }

        boolean isConfirmed;
        synchronized (this.connectionConfirmed) {
            isConfirmed = this.connectionConfirmed.get();
        }

        //only accept connection request as the first packet.
        if (!isConfirmed) {
            Logger.info("Client's first packet was not a PacketRequestConnection, closing connection...");
            ctx.close();
            return;
        }

        this.packetHandlers.get(packet.getType()).handlePacket(ctx, packet);
    }

    public ServerPacketHandler getClient() {
        return client;
    }

    public String getIp() {
        return ip;
    }

    public String getPassword() {
        return password;
    }

    public AtomicBoolean getConnectionConfirmed() {
        return connectionConfirmed;
    }

    public Map<PacketType, SubPacketHandler> getPacketHandlers() {
        return packetHandlers;
    }
}