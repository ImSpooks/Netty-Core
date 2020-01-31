package me.ImSpooks.rpg.multiplayer.client.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public interface CoreConnection {
    void start();
    void establish(Bootstrap bootstrap);
    void addCloseListener(Bootstrap bootstrap, Channel channel);
    void reconnect();
    void shutdown();

}