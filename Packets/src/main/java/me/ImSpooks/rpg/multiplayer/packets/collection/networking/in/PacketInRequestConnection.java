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
public class PacketInRequestConnection extends PacketIn {

    private String password;
    private int port;
    private String name;
    private UUID identification;

    public PacketInRequestConnection() {
    }

    public PacketInRequestConnection(String password, int port, String name, UUID identifier) {
        this.password = password;
        this.port = port;
        this.name = name;
        this.identification = identifier;
    }

    @Override
    public void send(WrappedOutputStream out) throws IOException {
        out.writeString(this.password);
        out.writeInt(this.port);
        out.writeString(this.name);
        out.writeUUID(this.identification);
    }

    @Override
    public void receive(WrappedInputStream in) throws IOException {
        this.password = in.readString();
        this.port = in.readInt();
        this.name = in.readString();
        this.identification = in.readUUID();
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public UUID getIdentification() {
        return identification;
    }
}