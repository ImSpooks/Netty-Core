package me.ImSpooks.nettycore.server.packets.handle;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInTest;
import me.ImSpooks.nettycore.packets.collection.networking.out.PacketOutConfirmConnection;
import me.ImSpooks.nettycore.packets.collection.networking.out.PacketOutForceDisconnect;
import me.ImSpooks.nettycore.packets.enums.DisconnectReason;
import me.ImSpooks.nettycore.server.Core;
import me.ImSpooks.nettycore.server.packets.PacketHandler;
import me.ImSpooks.nettycore.server.packets.PacketHandling;
import me.ImSpooks.nettycore.server.packets.SubPacketHandler;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.net.InetSocketAddress;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class NetworkPacketHandler extends SubPacketHandler {

    public NetworkPacketHandler(PacketHandler packetHandler, ServerSettings settings) {
        super(packetHandler, settings);
    }

    /**
     * Called when the client requests a connection
     *
     * @param ctx Client connection
     * @param packet PacketInRequestConnection
     */
    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketInRequestConnection packet) {
        String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

        // Whitelist check
        if (this.settings.getWhitelist().isEnabled()) {
            if (!this.settings.getWhitelist().getIps().contains(host)) {
                // Send disconnect packet with not whitelisted as reason
                ctx.writeAndFlush(new PacketOutForceDisconnect(DisconnectReason.NOT_WHITELISTED));
                Logger.info("Client ({}) on ip ({}) was disconnected because it is not whitelisted.", packet.getName(), host);
                return;
            }
        }
        // Blacklist check
        else if (this.settings.getBlacklist().isEnabled()) {
            if (this.settings.getBlacklist().getIps().contains(host)) {
                // Send disconnect packet with blacklisted as reason
                ctx.writeAndFlush(new PacketOutForceDisconnect(DisconnectReason.BLACKLISTED));
                Logger.info("Client ({}) on ip ({}) was disconnected because it is blacklisted.", packet.getName(), host);
                return;
            }
        }

        // Check if client is temporary blocked
        if (Core.getConnectionBuilder().getTempBlockedHosts().contains(host)) {
            Logger.info("Client ({}) tried to connect but was blocked from connecting, closing connection ({})...", packet.getName(), host);
            return;
        }

        // Check if password is wrong
        if (!packet.getPassword().equalsIgnoreCase(this.packetHandler.getPassword())) {
            // Max 5 attempts.
            int attempts = Core.getConnectionBuilder().getConnectionAttempts().getOrDefault(host, 0) + 1;
            Core.getConnectionBuilder().getConnectionAttempts().put(host, attempts);

            Logger.info("Client ({}) tried to connect with the wrong password, closing connection ({})... (Attempt: {})", packet.getName(), host, attempts);
            if (attempts >= 5) {
                Core.getConnectionBuilder().getTempBlockedHosts().add(host);
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