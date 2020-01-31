package me.ImSpooks.rpg.multiplayer.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import me.ImSpooks.core.Settings;
import me.ImSpooks.rpg.multiplayer.server.connection.ChannelBuilder;
import me.ImSpooks.rpg.multiplayer.server.settings.ServerSettings;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Core {

    private static ChannelBuilder CONNECTION_BUILDER;

    private static ServerSettings settings;

    public static void main(String[] args) {
        settings = null;
        try {
            settings = Settings.load(new File("config/server.json"), ServerSettings.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(8);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);

        // Allow a maximum of 32 threads
        EventExecutorGroup group = new DefaultEventExecutorGroup(32);
        CONNECTION_BUILDER = new ChannelBuilder(group, settings.password);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);


        bootstrap.childHandler(CONNECTION_BUILDER);

        try {
            bootstrap.bind(settings.port).sync();
        } catch (InterruptedException e) {
            Logger.error(e);
            return;
        }

        Logger.info("Started server on port {} and waiting for clients to connect...", settings.port);
    }

    /**
     * Returns the connection builder instance
     *
     * @return Channelbuilder instance
     */
    public static ChannelBuilder getConnectionBuilder() {
        return CONNECTION_BUILDER;
    }
}