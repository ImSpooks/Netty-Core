package me.ImSpooks.rpg.multiplayer.server.packets.handle;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.in.PacketInTest;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.out.PacketOutConfirmConnection;
import me.ImSpooks.rpg.multiplayer.server.Core;
import me.ImSpooks.rpg.multiplayer.server.packets.PacketHandler;
import me.ImSpooks.rpg.multiplayer.server.packets.PacketHandling;
import me.ImSpooks.rpg.multiplayer.server.packets.SubPacketHandler;
import org.tinylog.Logger;

import java.net.InetSocketAddress;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class NetworkPacketHandler extends SubPacketHandler {

    public NetworkPacketHandler(PacketHandler packetHandler) {
        super(packetHandler);
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketInRequestConnection packet) {
        String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

        if (Core.getConnectionBuilder().getBlockedHosts().contains(host)) {
            Logger.info("Client ({}) tried to connect but was blocked from connecting, closing connection ({})...", packet.getName(), host);
            return;
        }

        if (!packet.getPassword().equalsIgnoreCase(this.packetHandler.getPassword())) {
            int attempts = Core.getConnectionBuilder().getConnectionAttempts().getOrDefault(host, 0);
            attempts++;
            Logger.info("Client ({}) tried to connect with the wrong password, closing connection ({})... (Attempt: {})", packet.getName(), host, attempts);
            if (attempts >= 5) {
                Core.getConnectionBuilder().getBlockedHosts().add(host);
                Logger.info("Host \"{}\" is now blocked until next clear interval.", host);
            }
            ctx.close();
            return;
        }

        Logger.info("Password was correct. Client ({}) connected with IP {}...", packet.getName(), host);


        synchronized (this.packetHandler.getConnectionConfirmed()) {
            this.packetHandler.getConnectionConfirmed().set(true);
        }
        this.packetHandler.getClient().setIdentification(packet.getIdentification());
        this.packetHandler.getClient().setName(packet.getName());
        ctx.writeAndFlush(new PacketOutConfirmConnection());
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketInTest packet) {
        Logger.debug("Test packet received with and took {} milliseconds with id \"{}\"", System.currentTimeMillis() - packet.getTime(), packet.getUuid());
    }
}