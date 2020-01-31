package me.ImSpooks.rpg.multiplayer.server.connection;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.EventExecutorGroup;
import me.ImSpooks.rpg.multiplayer.packets.handle.coders.PacketDecoder;
import me.ImSpooks.rpg.multiplayer.packets.handle.coders.PacketEncoder;
import me.ImSpooks.rpg.multiplayer.server.packets.ServerPacketHandler;
import org.tinylog.Logger;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ChannelBuilder extends ChannelInitializer<SocketChannel> {

    private final Set<ServerPacketHandler> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> blockedHosts = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> connectionAttempts = Collections.synchronizedMap(new HashMap<>());


    private final EventExecutorGroup group;
    private final String password;

    public ChannelBuilder(EventExecutorGroup group, String password) {
        this.group = group;
        this.password = password;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        InetSocketAddress socketAddress = ch.remoteAddress();
        String ip = socketAddress.getAddress().getHostAddress();
        //TODO blocks and whitelisted ips

        Logger.debug("Client connected with ip {}", ip);

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));

        pipeline.addLast("packet_encoder", new PacketEncoder());
        pipeline.addLast("packet_decoder", new PacketDecoder());

        final ServerPacketHandler packetHandler = new ServerPacketHandler(ip, this.password);
        pipeline.addLast(this.group, "server_packet_handler", packetHandler);

        this.connectedClients.add(packetHandler);
        ch.closeFuture().addListener(future -> {
            disconnect(ip);
            connectedClients.remove(packetHandler);
        });

    }

    public Set<ServerPacketHandler> getConnectedClients() {
        return connectedClients;
    }

    public Map<String, Integer> getConnectionAttempts() {
        return connectionAttempts;
    }

    public Set<String> getBlockedHosts() {
        return blockedHosts;
    }

    public void disconnect(String ip) {
        Logger.info("Client on ip {} disconnected.", ip);
    }
}