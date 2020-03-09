package me.ImSpooks.nettycore.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.ImSpooks.nettycore.client.interfaces.CoreConnection;
import me.ImSpooks.nettycore.client.networking.CoreChannelInitializer;
import me.ImSpooks.nettycore.client.networking.IncomingListener;
import me.ImSpooks.nettycore.client.networking.PacketReceiver;
import me.ImSpooks.nettycore.client.packets.ClientPacketHandler;
import me.ImSpooks.nettycore.client.settings.ClientSettings;
import me.ImSpooks.nettycore.common.Settings;
import me.ImSpooks.nettycore.packets.collection.networking.PacketRequestConnection;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedChannel;
import me.ImSpooks.nettycore.packets.networking.CoreImplementation;
import me.ImSpooks.nettycore.packets.networking.PacketFlusher;
import org.apache.commons.cli.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    /**
     * Core client constructor
     *
     * @param settings Client settings instance
     */
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
    private PacketReceiver packetReceiver = new PacketReceiver();
    private ClientPacketHandler clientPacketHandler;


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

        this.workerGroup.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                getPacketReceiver().removeExpired();
            }
        }, 1, 1, TimeUnit.SECONDS);
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

                    packetFlusher.sendPacketHighPriority(new PacketRequestConnection(this.settings.getPassword(), this.settings.getPort(), this.settings.getName(), this.identifier));

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
            clientPacketHandler.setConnectionConfirmed(false);
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
     * @see PacketReceiver#addListener(Class, IncomingListener)
     */
    public <T extends Packet> void addIncomingListener(Class<T> packet, IncomingListener<T> incomingListener) {
        this.packetReceiver.addListener(packet, incomingListener);
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
     * Method to launch the client
     */
    public static CoreClient startClient(String[] args) {
        Options options = new Options();

        Option configPath = new Option("cp", "config-path", true, "Path to the config file");
        configPath.setRequired(false);
        options.addOption(configPath);

        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            Logger.error("Cannot parse vm options with paremeters {}", e.getMessage());
            formatter.printHelp("Core client", options);
            System.exit(1);
        }

        String path = "config/server.json";
        if (cmd.hasOption(configPath.getOpt())) {
            path = configPath.getValue();
        }


        ClientSettings settings;
        try {
            settings = Settings.load(new File(path), ClientSettings.class);
        } catch (FileNotFoundException e) {
            Logger.warn(e, "Unable to load settings file");
            settings = new ClientSettings();

            try {
                Settings.save(new File(path), settings);
            } catch (IOException ex) {
                Logger.error(ex, "Unable to save default settings.");
                System.exit(0);
            }
        }

        CoreClient coreClient = new CoreClient(settings);
        coreClient.start();
        return coreClient;
    }

    /**
     * Method to launch the client
     */
    public static CoreClient startClient() {
        return startClient(new String[0]);
    }



    // Getters
    /**
     * @return Client settings
     */
    public ClientSettings getSettings() {
        return settings;
    }

    /**
     * @return Identifier
     */
    public UUID getIdentifier() {
        return identifier;
    }

    /**
     * @return Client has stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * @return Action to perform when the client connects to the server
     */
    public Runnable getCoreConnected() {
        return coreConnected;
    }

    /**
     * @param coreConnected Action to perform when the clients connects to the server
     */
    public void setCoreConnected(Runnable coreConnected) {
        this.coreConnected = coreConnected;
    }

    /**
     * @return Is core connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * @return Wrapped channel to the server
     */
    public WrappedChannel getWrappedChannel() {
        return wrappedChannel;
    }

    /**
     * @return Client has started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return Packet flusher instance
     */
    public PacketFlusher getPacketFlusher() {
        return packetFlusher;
    }

    /**
     * @return Worker group instance
     */
    public NioEventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    /**
     * @return Client packet handler instance
     */
    public ClientPacketHandler getClientPacketHandler() {
        return clientPacketHandler;
    }

    /**
     * @return Packet receiver class
     */
    public PacketReceiver getPacketReceiver() {
        return packetReceiver;
    }

    public static CoreClient main(String[] args) {
        return startClient(args);
    }

}