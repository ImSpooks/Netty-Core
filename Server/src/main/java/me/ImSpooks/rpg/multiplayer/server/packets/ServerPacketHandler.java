package me.ImSpooks.rpg.multiplayer.server.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import org.tinylog.Logger;

import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ServerPacketHandler extends ChannelInboundHandlerAdapter {

    private final PacketHandler packetHandler;
    private final String password;
    private volatile ChannelHandlerContext latestCtx;

    private String name = "Unknown client";
    private volatile UUID identification = null;

    public ServerPacketHandler(String ip, String password) {
        this.packetHandler = new PacketHandler(this, ip, password);
        this.password = password;
    }

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

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getIdentification() {
        return identification;
    }

    public void setIdentification(UUID identification) {
        this.identification = identification;
    }

    public void sendPacket(Packet... packets) {
        assert packets.length != 0 : "Packet size must be atleast 1";

        for (Packet packet : packets) {
            this.latestCtx.write(packets);
        }
        this.latestCtx.flush();
    }
}