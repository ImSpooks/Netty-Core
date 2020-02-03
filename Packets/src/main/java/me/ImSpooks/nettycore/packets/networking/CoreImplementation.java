package me.ImSpooks.nettycore.packets.networking;

import me.ImSpooks.nettycore.packets.handle.channels.WrappedChannel;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public interface CoreImplementation {
    /**
     * @return Wrapped channel to the server
     */
    WrappedChannel getChannelUnsafe();
}