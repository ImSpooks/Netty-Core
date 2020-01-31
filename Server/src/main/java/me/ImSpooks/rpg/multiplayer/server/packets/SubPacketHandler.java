package me.ImSpooks.rpg.multiplayer.server.packets;

import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import me.ImSpooks.rpg.multiplayer.packets.handle.PacketRegister;
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

    protected PacketHandler packetHandler;

    private Map<Class<? extends Packet>, Method> METHODS = new HashMap<>();

    public SubPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;

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

    public boolean handlePacket(ChannelHandlerContext ctx, Packet packet) {
        if (!METHODS.containsKey(packet.getClass())) {
            Logger.warn("There is no packet handler found for packet \"{}\"", packet.getClass().getSimpleName());
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