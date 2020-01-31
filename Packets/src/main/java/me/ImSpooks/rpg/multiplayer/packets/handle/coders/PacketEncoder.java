package me.ImSpooks.rpg.multiplayer.packets.handle.coders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedOutputStream;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        try {
            WrappedOutputStream wrapped = new WrappedOutputStream(out);
            wrapped.write(msg.getId());
            msg.send(wrapped);

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