package me.ImSpooks.rpg.multiplayer.packets.collection.networking.out;

import me.ImSpooks.rpg.multiplayer.packets.handle.PacketOut;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketOutConfirmConnection extends PacketOut {

    public PacketOutConfirmConnection() {
    }

    @Override
    public void send(WrappedOutputStream out) throws IOException {

    }

    @Override
    public void receive(WrappedInputStream in) throws IOException {

    }
}