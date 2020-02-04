package me.ImSpooks.nettycore.server.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ServerPacketHandler extends ChannelInboundHandlerAdapter {

    // Variables
    private final PacketHandler packetHandler;
    private final ServerSettings settings;
    private volatile ChannelHandlerContext latestCtx;

    private String name = "Unknown client";
    private volatile UUID identification = null;
    private String ip;

    /**
     * Server packet handler instance for connected client
     *
     * @param ip Client ip
     * @param settings Server settings
     */
    public ServerPacketHandler(String ip, ServerSettings settings) {
        this.ip = ip;
        this.settings = settings;
        this.packetHandler = new PacketHandler(this, ip, settings);
    }

    /**
     * Called when the server receives a packet
     *
     * @param ctx Client connection
     * @param msg Packet as an object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.latestCtx = ctx;
        try {
            if (msg instanceof Packet) {
                this.packetHandler.handlePacket(ctx, (Packet) msg);
            }
        } catch (Throwable t) {
            Logger.error(t);
            throw t;
        }
    }

    /**
     * Called when an exception is thrown
     *
     * @param ctx Connection to client
     * @param cause Throw cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.warn(cause);
    }

    /**
     * @return Packet handler
     */
    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    /**
     * @return Server settings
     */
    public ServerSettings getSettings() {
        return settings;
    }

    /**
     * @return Client name
     */
    public String getName() {
        return name;
    }

    /**
     * Change the clients name
     *
     * @param name New client name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Client identification by a random uuid
     */
    public UUID getIdentification() {
        return identification;
    }

    public void setIdentification(UUID identification) {
        this.identification = identification;
    }

    /**
     * @return Client's ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * Send packets to the client
     *
     * @param packets Packets to send
     */
    public void sendPacket(Packet... packets) {
        assert packets.length != 0 : "Packet size must be atleast 1";

        for (Packet packet : packets) {
            this.latestCtx.write(packet);
        }
        this.latestCtx.flush();
    }
}