package me.ImSpooks.rpg.multiplayer.packets.collection.networking.in;

import me.ImSpooks.rpg.multiplayer.packets.handle.PacketIn;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.rpg.multiplayer.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketInTest extends PacketIn {

    private UUID uuid;
    private long time;

    public PacketInTest() {
    }

    public PacketInTest(UUID uuid) {
        this.uuid = uuid;
        this.time = System.currentTimeMillis();
    }

    @Override
    public void send(WrappedOutputStream out) throws IOException {
        out.writeUUID(this.uuid);
        out.writeLong(this.time);
    }

    @Override
    public void receive(WrappedInputStream in) throws IOException {
        this.uuid = in.readUUID();
        this.time = in.readLong();
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTime() {
        return time;
    }
}