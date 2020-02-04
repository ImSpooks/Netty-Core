package me.ImSpooks.nettycore.client.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.client.packets.handle.NetworkPacketHandler;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.PacketType;
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
    private String ip;
    private String password;
    private volatile UUID identification;
    private final ClientSettings settings;

    private final AtomicBoolean connectionConfirmed = new AtomicBoolean(false);

    /**
     * Packet handler instance for connected client
     *
     * @param settings Client settings
     */
    public PacketHandler(ClientSettings settings) {
        this.settings = settings;
        this.ip = settings.getTargetIp();
        this.password = settings.getPassword();

        for (Map.Entry<PacketType, List<Class<? extends SubPacketHandler>>> entry : packetHandlers.entrySet()) {
            try {
                localPacketHandlers.putIfAbsent(entry.getKey(), new ArrayList<>());

                for (Class<? extends SubPacketHandler> packetHandler : entry.getValue()) {
                    localPacketHandlers.get(entry.getKey()).add(packetHandler.getConstructor(PacketHandler.class, ClientSettings.class).newInstance(this, settings));
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Logger.warn(e);
            }
        }
    }

    private final Map<PacketType, List<SubPacketHandler>> localPacketHandlers = Collections.synchronizedMap(new HashMap<>());

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        boolean found = false;
        for (SubPacketHandler subPacketHandler : localPacketHandlers.get(packet.getPacketType())) {
            found = subPacketHandler.handlePacket(ctx, packet) || found;
        }
        if (!found) {
            Logger.warn("There is no packet handler found for packet \"{}\"", packet.getClass().getSimpleName());
        }
    }

    /**
     * @return Server Ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Atomic boolean for connection confirmed
     */
    public AtomicBoolean getConnectionConfirmed() {
        return connectionConfirmed;
    }

    /**
     * @return Sub packet handlers for each type
     */
    public Map<PacketType, List<SubPacketHandler>> getPacketHandlers() {
        return localPacketHandlers;
    }
}