package me.ImSpooks.nettycore.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import me.ImSpooks.nettycore.common.Settings;
import me.ImSpooks.nettycore.server.connection.ChannelBuilder;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.apache.commons.cli.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Core {

    // static Channel builder & static settings
    private static ChannelBuilder connectionBuilder;
    private final ServerSettings settings;

    /**
     * Core constructor
     *
     * @param settings Server settings instance
     */
    public Core(ServerSettings settings) {
        this.settings = settings;

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(8);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);

        // Allow a maximum of 32 threads
        EventExecutorGroup group = new DefaultEventExecutorGroup(32);
        connectionBuilder = new ChannelBuilder(group, settings);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.childHandler(connectionBuilder);

        try {
            bootstrap.bind(settings.getPort()).sync();
        } catch (InterruptedException e) {
            Logger.error(e);
            return;
        }

        Logger.info("Started server on port {}, wating for connections...", settings.getPort());

    }

    /**
     * Method to launch the server
     */
    public static Core startServer(String[] args) {
        Options options = new Options();

        Option configPath = new Option("cp", "config-path", true, "Path to the config file");
        configPath.setRequired(false);
        options.addOption(configPath);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            Logger.error("Cannot parse vm options with paremeters {}", e.getMessage());
            formatter.printHelp("Core server", options);
            System.exit(1);
        }

        String path = "config/server.json";
        if (cmd.hasOption(configPath.getOpt())) {
            path = configPath.getValue();
        }


        ServerSettings settings;
        try {
            settings = Settings.load(new File(path), ServerSettings.class);
        } catch (Exception e) {
            Logger.warn(e, "Unable to load settings file");
            settings = new ServerSettings();

            try {
                Settings.save(new File(path), settings);
            } catch (IOException ex) {
                Logger.error(ex, "Unable to save default settings.");
                System.exit(0);
            }
        }

        return new Core(settings);
    }
    /**
     * Method to launch the server
     */
    public static Core startServer() {
        return startServer(new String[0]);
    }

    /**
     * @return {@code static} Channel builder instance
     */
    public static ChannelBuilder getConnectionBuilder() {
        return connectionBuilder;
    }

    /**
     * @return Core server settings
     */
    public ServerSettings getSettings() {
        return this.settings;
    }

    public static void main(String[] args) {
        startServer(args);
    }
}