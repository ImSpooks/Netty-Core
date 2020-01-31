package me.ImSpooks.rpg.multiplayer.packets.handle.coders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import me.ImSpooks.rpg.multiplayer.packets.handle.PacketRegister;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedInputStream;
import org.tinylog.Logger;

import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            WrappedInputStream wrapper = new WrappedInputStream(in);

            int packetId = wrapper.read();
            Packet packet = PacketRegister.createInstance(packetId);

            packet.setPacketType(PacketRegister.getPacketType(packetId));
            packet.receive(wrapper);

            out.add(packet);
        } catch (Throwable t) {
            if (t instanceof IndexOutOfBoundsException) return;
            Logger.error(t);
            throw t;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error(cause);
        ctx.close();
    }
}