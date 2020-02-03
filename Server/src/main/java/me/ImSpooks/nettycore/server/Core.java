package me.ImSpooks.nettycore.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import me.ImSpooks.nettycore.common.Settings;
import me.ImSpooks.nettycore.server.connection.ChannelBuilder;
import me.ImSpooks.nettycore.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Core {

    // static Channel builder & static settings
    private static ChannelBuilder CONNECTION_BUILDER;
    private static ServerSettings SETTINGS;

    /**
     * Method to launch to program
     */
    public static void main(String[] args) {
        SETTINGS = null;
        try {
            SETTINGS = Settings.load(new File("config/server.json"), ServerSettings.class);
        } catch (FileNotFoundException e) {
            Logger.warn(e, "Unable to load settings file");
            SETTINGS = new ServerSettings();

            try {
                Settings.save(new File("config/server.json"), SETTINGS);
            } catch (IOException ex) {
                Logger.error(ex, "Unable to save default settings.");
                System.exit(0);
            }
        }

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(8);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);

        // Allow a maximum of 32 threads
        EventExecutorGroup group = new DefaultEventExecutorGroup(32);
        CONNECTION_BUILDER = new ChannelBuilder(group, SETTINGS);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.childHandler(CONNECTION_BUILDER);

        try {
            bootstrap.bind(SETTINGS.getPort()).sync();
        } catch (InterruptedException e) {
            Logger.error(e);
            return;
        }

        Logger.info("Started server on port {}, wating for connections...", SETTINGS.getPort());
    }

    /**
     * @return Channelbuilder instance
     */
    public static ChannelBuilder getConnectionBuilder() {
        return CONNECTION_BUILDER;
    }

    /**
     * @return Core server settings
     */
    public static ServerSettings getSettings() {
        return SETTINGS;
    }
}