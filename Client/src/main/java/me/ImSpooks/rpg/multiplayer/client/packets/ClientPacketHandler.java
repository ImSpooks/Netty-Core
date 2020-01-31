package me.ImSpooks.rpg.multiplayer.client.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.ImSpooks.rpg.multiplayer.client.CoreClient;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.out.PacketOutConfirmConnection;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ClientPacketHandler extends ChannelInboundHandlerAdapter {

    private final PacketHandler packetHandler;
    private final CoreClient coreClient;
    private volatile boolean connectionConfirmed = false;

    public ClientPacketHandler(CoreClient coreClient) {
        this.coreClient = coreClient;
        this.packetHandler = new PacketHandler(coreClient.getIp(), coreClient.getPassword());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;

        if (connectionConfirmed) {
            this.packetHandler.handlePacket(ctx, packet);
        } else {
            if (!(packet instanceof PacketOutConfirmConnection)) {
                Logger.info("First packet was {} but had to be PacketOutConfirmConnection, disconnecting...", packet.getClass().getSimpleName());
                this.coreClient.reconnect();
                return;
            }

            Logger.info("Connection confirmed, packets can now be send...");

            if (this.coreClient.getCoreConnected() != null)
                this.coreClient.getCoreConnected().run();
            this.connectionConfirmed = true;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.error(cause);
    }
}