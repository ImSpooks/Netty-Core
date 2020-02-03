package me.ImSpooks.nettycore.server.connection;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.EventExecutorGroup;
import me.ImSpooks.nettycore.packets.handle.coders.PacketDecoder;
import me.ImSpooks.nettycore.packets.handle.coders.PacketEncoder;
import me.ImSpooks.nettycore.server.packets.ServerPacketHandler;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ChannelBuilder extends ChannelInitializer<SocketChannel> {

    private final Set<ServerPacketHandler> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> tempBlockedHosts = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> connectionAttempts = Collections.synchronizedMap(new HashMap<>());

    // Variables
    private final EventExecutorGroup group;
    private final ServerSettings settings;

    /**
     * Channel builder instance
     *
     * @param group Work group
     * @param settings Server settings
     */
    public ChannelBuilder(EventExecutorGroup group, ServerSettings settings) {
        this.group = group;
        this.settings = settings;

        // Thread to clear attempts & blocked list
        new Thread(new Runnable() {
            int ticks = 0;
            @Override
            public void run() {
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (ticks++ % 5 == 0) {
                    tempBlockedHosts.clear();
                    connectionAttempts.clear();

                    Logger.info("Temporary blocked hosts and attempts map cleared.");
                }
                else {
                    Logger.info("Attempts map cleared.");
                    connectionAttempts.clear();
                }


            }
        }, "Client unblocker").start();
    }

    /**
     * Called when a client connects to the server
     *
     * @param ch Client's socket channel
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        InetSocketAddress socketAddress = ch.remoteAddress();
        String ip = socketAddress.getAddress().getHostAddress();

        Logger.debug("Client connected with ip {}", ip);

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));

        pipeline.addLast("packet_encoder", new PacketEncoder());
        pipeline.addLast("packet_decoder", new PacketDecoder());

        final ServerPacketHandler packetHandler = new ServerPacketHandler(ip, this.settings);
        pipeline.addLast(this.group, "server_packet_handler", packetHandler);

        this.connectedClients.add(packetHandler);
        ch.closeFuture().addListener(future -> {
            disconnect(ip);
            connectedClients.remove(packetHandler);
        });

    }

    /**
     * @return Connected clients as a Set
     */
    public Set<ServerPacketHandler> getConnectedClients() {
        return connectedClients;
    }

    /**
     * @return Connection attempts as a Map<String, Integer>
     */
    public Map<String, Integer> getConnectionAttempts() {
        return connectionAttempts;
    }

    /**
     * @return Blocked hosts as a set
     */
    public Set<String> getTempBlockedHosts() {
        return tempBlockedHosts;
    }


    /**
     * Client that disconnected on a certain ip
     *
     * @param ip Ip that disconnected
     */
    public void disconnect(String ip) {
        Logger.info("Client on ip {} disconnected.", ip);
    }
}