package me.ImSpooks.rpg.multiplayer.client.networking;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import me.ImSpooks.rpg.multiplayer.client.CoreClient;
import me.ImSpooks.rpg.multiplayer.client.packets.ClientPacketHandler;
import me.ImSpooks.rpg.multiplayer.packets.handle.coders.PacketDecoder;
import me.ImSpooks.rpg.multiplayer.packets.handle.coders.PacketEncoder;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class CoreChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final CoreClient coreClient;

    public CoreChannelInitializer(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));

        pipeline.addLast("packet_encoder", new PacketEncoder());
        pipeline.addLast("packet_decoder", new PacketDecoder());
        pipeline.addLast("packet_handler", new ClientPacketHandler(this.coreClient));
    }
}