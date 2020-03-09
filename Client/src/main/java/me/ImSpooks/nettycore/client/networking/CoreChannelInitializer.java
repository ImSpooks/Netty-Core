package me.ImSpooks.nettycore.client.networking;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import me.ImSpooks.nettycore.client.CoreClient;
import me.ImSpooks.nettycore.client.packets.ClientPacketHandler;
import me.ImSpooks.nettycore.packets.handle.coders.PacketDecoder;
import me.ImSpooks.nettycore.packets.handle.coders.PacketEncoder;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class CoreChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final CoreClient coreClient;

    /**
     * Core channel initializer
     *
     * @param coreClient Client instance
     */
    public CoreChannelInitializer(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    /**
     * Called when a connection to the server was made
     *
     * @param ch Server's socket channel
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));

        pipeline.addLast("packet_encoder", new PacketEncoder());
        pipeline.addLast("packet_decoder", new PacketDecoder());
        pipeline.addLast("packet_handler", new ClientPacketHandler(this.coreClient));
    }
}