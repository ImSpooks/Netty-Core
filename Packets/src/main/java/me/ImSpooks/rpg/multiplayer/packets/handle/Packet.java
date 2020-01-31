package me.ImSpooks.rpg.multiplayer.packets.handle;

import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public abstract class Packet {

    private PacketType packetType;

    public abstract void send(WrappedOutputStream out) throws IOException;
    public abstract void receive(WrappedInputStream in) throws IOException;

    public int getId() {
        return PacketRegister.getId(this);
    }

    public PacketType getType() {
        return PacketRegister.getPacketType(this.getId());
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }
}