package me.ImSpooks.nettycore.packets.collection.networking;

import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketConfirmConnection extends Packet {

    public PacketConfirmConnection() {
    }

    @Override
    public void send(WrappedOutputStream out) throws IOException {
    }

    @Override
    public void receive(WrappedInputStream in) throws IOException {
    }
}