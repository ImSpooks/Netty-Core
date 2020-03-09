package me.ImSpooks.nettycore.client.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.ImSpooks.nettycore.client.CoreClient;
import me.ImSpooks.nettycore.packets.collection.networking.PacketConfirmConnection;
import me.ImSpooks.nettycore.packets.handle.Packet;
import org.tinylog.Logger;

/**
 * Created by Nick on 9 Mar 2020.
 * Copyright © ImSpooks
 */
public class ClientPacketHandler extends ChannelInboundHandlerAdapter {

    private final CoreClient coreClient;
    private volatile boolean connectionConfirmed;

    /**
     * Server packet handler instance for connected client
     *
     * @param coreClient Client instance
     */
    public ClientPacketHandler(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    /**
     * Called when the client receives a packet
     *
     * @param ctx Client connection
     * @param msg Packet as an object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        if (this.connectionConfirmed) {
            this.coreClient.getPacketReceiver().received(packet);
        } else {
            if (!(packet instanceof PacketConfirmConnection)) {
                Logger.info("First packet was {} but had to be PacketConfirmConnection, disconnecting...", packet.getClass().getSimpleName());
                this.coreClient.reconnect();
                return;
            }
            PacketConfirmConnection correct = (PacketConfirmConnection) packet;

            Logger.info("Connection confirmed, packets can now be send...");
            if(this.coreClient.getCoreConnected() != null) {
                this.coreClient.getCoreConnected().run();
            }
            this.coreClient.getPacketReceiver().connectionEstablished();
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
     * @return {@code true} when connection has been confirmed, {@code false} otherwise
     */
    public boolean isConnectionConfirmed() {
        return connectionConfirmed;
    }

    /**
     * @param connectionConfirmed Set if connection has been confirmed
     */
    public void setConnectionConfirmed(boolean connectionConfirmed) {
        this.connectionConfirmed = connectionConfirmed;
    }
}
