package me.ImSpooks.rpg.multiplayer.packets.networking;

import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedChannel;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public interface CoreImplementation {
    WrappedChannel getChannelUnsafe();
}