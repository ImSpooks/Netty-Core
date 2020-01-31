package me.ImSpooks.rpg.multiplayer.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.ImSpooks.rpg.common.helpers.JsonConfig;
import me.ImSpooks.rpg.multiplayer.client.interfaces.CoreConnection;
import me.ImSpooks.rpg.multiplayer.client.networking.CoreChannelInitializer;
import me.ImSpooks.rpg.multiplayer.client.packets.ClientPacketHandler;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.rpg.multiplayer.packets.collection.networking.in.PacketInTest;
import me.ImSpooks.rpg.multiplayer.packets.handle.Packet;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedChannel;
import me.ImSpooks.rpg.multiplayer.packets.networking.CoreImplementation;
import me.ImSpooks.rpg.multiplayer.packets.networking.PacketFlusher;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class CoreClient implements CoreImplementation, CoreConnection {

    private static final long RECONNECT_INTERVAL = 5000L;

    private static final Random random = new Random(System.currentTimeMillis());

    private final UUID identifier = UUID.randomUUID();
    private final String ip;
    private final int port;
    private final String password;
    private final String name;
    private volatile boolean stopped = false;
    private volatile Runnable coreConnected = () -> {};
    private boolean isConnected = false;

    public CoreClient(String ip, int port, String password, String name) {
        if (port < 1 || port > (Short.MAX_VALUE + 1) * 2)
            throw new IllegalArgumentException("Port value out of range (" + port + ")");

        if (ip == null)
            throw new IllegalArgumentException("Target IP may not be null");

        if (password.length() < 5)
            throw new IllegalArgumentException("Password must be longer then 5 characters");

        this.ip = ip;
        this.port = port;
        this.password = password;
        this.name = name;
    }

    private final WrappedChannel wrappedChannel = new WrappedChannel(null);

    private boolean started = false;
    private final PacketFlusher packetFlusher = new PacketFlusher(this);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);
    private ClientPacketHandler serverPacketHandler;

    @Override
    public void start() {
        if (this.started)
            throw new UnsupportedOperationException("The client has already started.");

        this.started = true;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new CoreChannelInitializer(this));

        establish(bootstrap);
    }

    @Override
    public void establish(Bootstrap bootstrap) {
        try {
            if (this.stopped) {
                return;
            }

            Logger.info("Establishing connection... {}:{}", this.ip, this.port);
            final ChannelFuture tmp = bootstrap.connect(this.ip, this.port);

            tmp.addListener(future -> {
                if (future.isSuccess()) {
                    wrappedChannel.lock();
                    try {
                        Channel channel = tmp.channel();
                        wrappedChannel.set(channel);
                        addCloseListener(bootstrap, channel);
                    } finally {
                        wrappedChannel.unlock();
                    }

                    Logger.info("Connection established, confirming identity...");

                    packetFlusher.sendPacketHighPriority(new PacketInRequestConnection(this.password, this.port, this.name, this.identifier));

                    Logger.info("Connection verification requested.");
                    isConnected = true;
                } else {
                    Logger.info("No connection established, trying again in {} seconds...", (RECONNECT_INTERVAL / 1000L));
                    isConnected = false;
                    Thread.sleep(RECONNECT_INTERVAL);
                    establish(bootstrap);
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void addCloseListener(Bootstrap bootstrap, Channel channel) {
        channel.closeFuture().addListener(future -> {
            wrappedChannel.lock();
            if (wrappedChannel.get() == null) {
                wrappedChannel.unlock();
                return;
            }
            wrappedChannel.set(null);
            wrappedChannel.unlock();

            Logger.warn("Connection lost... Trying again in {} seconds...", (RECONNECT_INTERVAL / 1000L));
            isConnected = false;
            Thread.sleep(RECONNECT_INTERVAL);
            establish(bootstrap);
        });
    }

    @Override
    public void reconnect() {
        wrappedChannel.lock();
        try {
            Channel ch = wrappedChannel.get();
            if (ch != null)
                ch.close();
        } finally {
            wrappedChannel.unlock();
        }
    }

    @Override
    public void shutdown() {
        this.workerGroup.shutdownGracefully();
        this.stopped = true;
        wrappedChannel.lock();
        try {
            Channel ch = wrappedChannel.get();
            if (ch != null)
                ch.close();
        } finally {
            wrappedChannel.unlock();
        }
    }

    public void sendPacket(Packet... packets) {
        this.packetFlusher.sendPacket(packets);
    }

    @Override
    public WrappedChannel getChannelUnsafe() {
        return wrappedChannel;
    }

    public static void main(String[] args) {
        JsonConfig clientDetails = null;
        try {
            clientDetails = new JsonConfig("config/client.json");
        } catch (IOException e) {
            Logger.error(e, "Couldn't load settings.");
            return;
        }

        clientDetails.expect("targetip", "127.0.0.1");
        clientDetails.expect("port", 7000);
        clientDetails.expect("name", "INSERT_NAME");
        clientDetails.expect("password", "INSERT_PWD");

        CoreClient coreClient = new CoreClient(
                clientDetails.getString("targetip"),
                clientDetails.getInt("port"),
                clientDetails.getString("password"),
                clientDetails.getString("name")

        );

        coreClient.setCoreConnected(() -> {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    coreClient.sendPacket(new PacketInTest(UUID.randomUUID()));
                }
            }).start();
        });

        coreClient.start();
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Runnable getCoreConnected() {
        return coreConnected;
    }

    public void setCoreConnected(Runnable coreConnected) {
        this.coreConnected = coreConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public WrappedChannel getWrappedChannel() {
        return wrappedChannel;
    }

    public boolean isStarted() {
        return started;
    }

    public PacketFlusher getPacketFlusher() {
        return packetFlusher;
    }

    public NioEventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public ClientPacketHandler getServerPacketHandler() {
        return serverPacketHandler;
    }
}