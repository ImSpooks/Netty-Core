package me.ImSpooks.nettycore.client.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.ImSpooks.nettycore.client.CoreClient;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.packets.collection.networking.out.PacketOutConfirmConnection;
import me.ImSpooks.nettycore.packets.collection.networking.out.PacketOutForceDisconnect;
import me.ImSpooks.nettycore.packets.handle.Packet;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ClientPacketHandler extends ChannelInboundHandlerAdapter {

    // Variables
    private final PacketHandler packetHandler;
    private final CoreClient coreClient;
    private volatile boolean connectionConfirmed = false;

    /**
     * Server packet handler instance for connected client
     *
     * @param coreClient Client instance
     * @param settings Server settings
     */
    public ClientPacketHandler(CoreClient coreClient, ClientSettings settings) {
        this.coreClient = coreClient;
        this.packetHandler = new PacketHandler(settings);
    }

    /**
     * Called when the client receives a packet
     *
     * @param ctx Client connection
     * @param msg Packet as an object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;

        if (packet instanceof PacketOutForceDisconnect) {
            this.packetHandler.handlePacket(ctx, packet);
            return;
        }

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

    /**
     * Called when an exception is thrown
     *
     * @param ctx Connection to server
     * @param cause Throw cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error(cause);
    }

    /**
     * @return Packet handler
     */
    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    /**
     * @return Core client instance
     */
    public CoreClient getCoreClient() {
        return coreClient;
    }

    /**
     * @return Is connection confirmed
     */
    public boolean isConnectionConfirmed() {
        return connectionConfirmed;
    }
}