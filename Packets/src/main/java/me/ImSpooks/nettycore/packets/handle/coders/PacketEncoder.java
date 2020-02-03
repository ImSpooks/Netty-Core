package me.ImSpooks.nettycore.packets.handle.coders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedOutputStream;
import org.tinylog.Logger;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    /**
     * Encodes the packet
     *
     * @param ctx Connection
     * @param msg Packet to send
     * @param out Output
     * @throws Exception when an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        try {
            WrappedOutputStream wrapped = new WrappedOutputStream(out);
            wrapped.writeInt(msg.getId());
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