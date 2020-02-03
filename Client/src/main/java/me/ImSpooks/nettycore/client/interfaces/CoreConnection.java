package me.ImSpooks.nettycore.client.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public interface CoreConnection {
    /**
     * Called when the client starts
     */
    void start();

    /**
     * Called when trying to establish a connection to the server
     *
     * @param bootstrap Connection bootstrap
     */
    void establish(Bootstrap bootstrap);

    /**
     * Adds a close listener when the connection is closed
     *
     * @param bootstrap Connection bootstrap
     * @param channel Channel to server
     */
    void addCloseListener(Bootstrap bootstrap, Channel channel);

    /**
     * Called when trying to reconnect
     */
    void reconnect();

    /**
     * Called on shutdown
     */
    void shutdown();

}