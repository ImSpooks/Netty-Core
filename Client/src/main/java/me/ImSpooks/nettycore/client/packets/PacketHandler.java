package me.ImSpooks.nettycore.client.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.client.packets.handle.NetworkPacketHandler;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.PacketType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Nick on 02 okt. 2019.
 * Copyright © ImSpooks
 */
public class PacketHandler {

    // Variables
    private String ip;
    private String password;
    private volatile UUID identification;
    private final ClientSettings settings;

    private final AtomicBoolean connectionConfirmed = new AtomicBoolean(false);

    /**
     * Packet handler instance for connected client
     *
     * @param settings Client settings
     */
    public PacketHandler(ClientSettings settings) {
        this.settings = settings;
        this.ip = settings.getTargetIp();
        this.password = settings.getPassword();

        this.packetHandlers.put(PacketType.NETWORKING, new NetworkPacketHandler(this, settings));
    }

     private final Map<PacketType, SubPacketHandler> packetHandlers = Collections.synchronizedMap(new HashMap<>());

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        this.packetHandlers.get(packet.getPacketType()).handlePacket(ctx, packet);
    }

    /**
     * @return Server Ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Atomic boolean for connection confirmed
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