package me.ImSpooks.nettycore.server.packets.handle;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInTest;
import me.ImSpooks.nettycore.server.packets.PacketHandler;
import me.ImSpooks.nettycore.server.packets.PacketHandling;
import me.ImSpooks.nettycore.server.packets.SubPacketHandler;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

/**
 * Created by Nick on 04 feb. 2020.
 * Copyright © ImSpooks
 */
public class TestPacketHandler extends SubPacketHandler {

    public TestPacketHandler(PacketHandler packetHandler, ServerSettings settings) {
        super(packetHandler, settings);
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketInTest packet) {
        Logger.info(new Gson().toJson(packet));
    }
}