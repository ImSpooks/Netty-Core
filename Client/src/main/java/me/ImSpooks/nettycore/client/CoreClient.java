package me.ImSpooks.nettycore.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.ImSpooks.nettycore.client.interfaces.CoreConnection;
import me.ImSpooks.nettycore.client.networking.CoreChannelInitializer;
import me.ImSpooks.nettycore.client.packets.ClientPacketHandler;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.common.Settings;
import me.ImSpooks.nettycore.packets.collection.networking.in.PacketInRequestConnection;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedChannel;
import me.ImSpooks.nettycore.packets.networking.CoreImplementation;
import me.ImSpooks.nettycore.packets.networking.PacketFlusher;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileNotFoundException;
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

    // Variables
    private final UUID identifier = UUID.randomUUID();
    private ClientSettings settings;
    private volatile boolean stopped = false;
    private volatile Runnable coreConnected = () -> {};
    private boolean isConnected = false;

    public CoreClient(ClientSettings settings) {
        this.settings = settings;
        int port = settings.getPort();
        String ip = settings.getTargetIp();
        String password = settings.getPassword();

        if (port < 1 || port > (Short.MAX_VALUE + 1) * 2)
            throw new IllegalArgumentException("Port value out of range (" + port + ")");

        if (ip == null)
            throw new IllegalArgumentException("Target IP may not be null");

        if (password.length() < 5)
            throw new IllegalArgumentException("Password must be longer then 5 characters");

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private final WrappedChannel wrappedChannel = new WrappedChannel(null);

    // Variables
    private boolean started = false;
    private final PacketFlusher packetFlusher = new PacketFlusher(this);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);
    private ClientPacketHandler serverPacketHandler;

    /**
     * @see CoreConnection#start()
     */
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

    /**
     * @see CoreConnection#establish(Bootstrap)
     */
    @Override
    public void establish(Bootstrap bootstrap) {
        try {
            if (this.stopped) {
                return;
            }

            Logger.info("Establishing connection... {}:{}", this.settings.getTargetIp(), this.settings.getPort());
            final ChannelFuture tmp = bootstrap.connect(this.settings.getTargetIp(), this.settings.getPort());

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

                    packetFlusher.sendPacketHighPriority(new PacketInRequestConnection(this.settings.getPassword(), this.settings.getPort(), this.settings.getName(), this.identifier));

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

    /**
     * @see CoreConnection#addCloseListener(Bootstrap, Channel)
     */
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

    /**
     * @see CoreConnection#reconnect()
     */
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

    /**
     * @see CoreConnection#shutdown()
     */
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

    /**
     * @see PacketFlusher#sendPacket(Packet...)
     */
    public void sendPacket(Packet... packets) {
        this.packetFlusher.sendPacket(packets);
    }

    /**
     * @see CoreImplementation#getChannelUnsafe()
     */
    @Override
    public WrappedChannel getChannelUnsafe() {
        return wrappedChannel;
    }

    /**
     * Method to launch the server
     */
    public static void main(String[] args) {
        ClientSettings settings;
        try {
            settings = Settings.load(new File("config/server.json"), ClientSettings.class);
        } catch (FileNotFoundException e) {
            Logger.warn(e, "Unable to load settings file");
            settings = new ClientSettings();

            try {
                Settings.save(new File("config/server.json"), settings);
            } catch (IOException ex) {
                Logger.error(ex, "Unable to save default settings.");
                System.exit(0);
            }
        }

        CoreClient coreClient = new CoreClient(settings);

/*        coreClient.setCoreConnected(() -> {
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
        });*/

        coreClient.start();
    }

    public ClientSettings getSettings() {
        return settings;
    }

    public UUID getIdentifier() {
        return identifier;
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