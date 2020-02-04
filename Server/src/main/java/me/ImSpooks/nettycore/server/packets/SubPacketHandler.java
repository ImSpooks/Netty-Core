package me.ImSpooks.nettycore.server.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.PacketRegister;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick on 01 okt. 2019.
 * Copyright © ImSpooks
 */
public class SubPacketHandler {

    protected final PacketHandler packetHandler;
    protected final ServerSettings settings;

    private Map<Class<? extends Packet>, Method> METHODS = new HashMap<>();

    /**
     * Sub packet handler instance
     *
     * @param packetHandler Parent packet handler
     * @param settings Server settings
     */
    public SubPacketHandler(PacketHandler packetHandler, ServerSettings settings) {
        this.packetHandler = packetHandler;
        this.settings = settings;

        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PacketHandling.class))
                continue;

            for (Class<? extends Packet> packet : PacketRegister.getPackets()) {
                if (method.getParameterTypes().length != 2) {
                    Logger.warn("Method must have 2 parameters (ChannelHandlerContext, Packet)");
                    continue;
                }

                if (method.getParameterTypes()[1] == Packet.class)
                    continue;

                if (method.getParameterTypes()[0] != ChannelHandlerContext.class) {
                    Logger.warn("First parameter type must be ChannelHandlerContext. ({})", method.getParameterTypes()[0].getSimpleName());
                    continue;
                }

                if (method.getParameterTypes()[1] == packet) {
                    method.setAccessible(true);
                    METHODS.put(packet, method);
                }
            }
        }
    }

    /**
     * Handles an incoming packet.
     * @param ctx Client connection
     * @param packet Incoming packet
     * @return {@code true} if packet was read succesfully, {@code false} otherwise
     */
    public boolean handlePacket(ChannelHandlerContext ctx, Packet packet) {
        if (!METHODS.containsKey(packet.getClass())) {
            return false;
        }

        try {
            METHODS.get(packet.getClass()).invoke(this, ctx, packet);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Method not found
            Logger.error(e, "There was en error thrown while invoking handling method for packet \"{}\"", packet.getClass().getName());
            return false;
        }
    }
}