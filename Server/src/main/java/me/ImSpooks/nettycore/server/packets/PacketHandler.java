package me.ImSpooks.nettycore.server.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.PacketType;
import me.ImSpooks.nettycore.server.packets.handle.NetworkPacketHandler;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Nick on 02 okt. 2019.
 * Copyright © ImSpooks
 */
public class PacketHandler {

    private static final Map<PacketType, List<Class<? extends SubPacketHandler>>> packetHandlers = new HashMap<>();

    static {
        addPacketHandler(PacketType.NETWORKING, NetworkPacketHandler.class);
    }

    public static void addPacketHandler(PacketType packetType, Class<? extends SubPacketHandler> packetHandler) {
        packetHandlers.putIfAbsent(packetType, new ArrayList<>());
        packetHandlers.get(packetType).add(packetHandler);
    }

    // Variables
    private final ServerPacketHandler client;
    private final ServerSettings settings;
    private final String ip;
    private final String password;

    private final AtomicBoolean connectionConfirmed = new AtomicBoolean(false);

    /**
     * Packet handler instance for connected client
     *
     * @param serverPacketHandler Server packet handler
     * @param ip Client ip
     * @param settings Server settings
     */
    public PacketHandler(ServerPacketHandler serverPacketHandler, String ip, ServerSettings settings) {
        this.client = serverPacketHandler;
        this.settings = settings;
        this.ip = ip;
        this.password = settings.getPassword();

        for (Map.Entry<PacketType, List<Class<? extends SubPacketHandler>>> entry : packetHandlers.entrySet()) {
            try {
                localPacketHandlers.putIfAbsent(entry.getKey(), new ArrayList<>());

                for (Class<? extends SubPacketHandler> packetHandler : entry.getValue()) {
                    localPacketHandlers.get(entry.getKey()).add(packetHandler.getConstructor(PacketHandler.class, ServerSettings.class).newInstance(this, settings));
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Logger.warn(e);
            }
        }
    }

    private final Map<PacketType, List<SubPacketHandler>> localPacketHandlers = Collections.synchronizedMap(new HashMap<>());

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PacketInRequestConnection) {
            this.localPacketHandlers.get(PacketType.NETWORKING).get(0).handlePacket(ctx, packet);
            return;
        }

        boolean isConfirmed;
        synchronized (this.connectionConfirmed) {
            isConfirmed = this.connectionConfirmed.get();
        }

        //only accept connection request as the first packet.
        if (!isConfirmed) {
            Logger.info("Client's first packet was not a PacketInRequestConnection but was \"" + packet.getClass().getSimpleName() + "\" instead. Closing connection...");
            ctx.close();
            return;
        }

        boolean found = false;
        for (SubPacketHandler subPacketHandler : localPacketHandlers.get(packet.getPacketType())) {
            found = subPacketHandler.handlePacket(ctx, packet) || found;
        }
        if (!found) {
            Logger.warn("There is no packet handler found for packet \"{}\"", packet.getClass().getSimpleName());
        }
    }

    /**
     * @return Server packet handler for client
     */
    public ServerPacketHandler getClient() {
        return client;
    }

    /**
     * @return Client's IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return Server password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Atomic boolean for confirmed connection
     */
    public AtomicBoolean getConnectionConfirmed() {
        return connectionConfirmed;
    }

    /**
     * @return Sub packet handlers for each type
     */
    public Map<PacketType, List<SubPacketHandler>> getLocalPacketHandlers() {
        return localPacketHandlers;
    }
}