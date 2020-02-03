package me.ImSpooks.nettycore.client.packets.handle;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.client.packets.PacketHandler;
import me.ImSpooks.nettycore.client.packets.PacketHandling;
import me.ImSpooks.nettycore.client.packets.SubPacketHandler;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.packets.collection.networking.out.PacketOutForceDisconnect;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class NetworkPacketHandler extends SubPacketHandler {

    public NetworkPacketHandler(PacketHandler packetHandler, ClientSettings settings) {
        super(packetHandler, settings);
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketOutForceDisconnect packet) {
        switch (packet.getReason()) {
            default:
            case UNKNOWN:
                Logger.warn("Client disconnected with an unknown reason.");
                break;
            case BLACKLISTED:
                Logger.warn("Client is blacklisted.");
                break;
            case NOT_WHITELISTED:
                Logger.warn("Client is not whitelisted.");
                break;
        }
        ctx.close();
    }

}