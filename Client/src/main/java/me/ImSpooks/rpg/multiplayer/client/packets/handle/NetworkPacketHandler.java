package me.ImSpooks.rpg.multiplayer.client.packets.handle;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.rpg.multiplayer.client.packets.PacketHandler;
import me.ImSpooks.rpg.multiplayer.client.packets.PacketHandling;
import me.ImSpooks.rpg.multiplayer.client.packets.SubPacketHandler;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.out.PacketOutConfirmConnection;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class NetworkPacketHandler extends SubPacketHandler {

    public NetworkPacketHandler(PacketHandler packetHandler) {
        super(packetHandler);
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketOutConfirmConnection packet) {
        Logger.info("Connection has been confirmed PogU");
    }
}