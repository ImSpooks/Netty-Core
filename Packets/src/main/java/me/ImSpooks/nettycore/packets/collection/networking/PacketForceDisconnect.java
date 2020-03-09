package me.ImSpooks.nettycore.packets.collection.networking;

import me.ImSpooks.nettycore.packets.enums.DisconnectReason;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;

/**
 * Created by Nick on 03 feb. 2020.
 * Copyright © ImSpooks
 */
public class PacketForceDisconnect extends Packet {

    // Reason
    private DisconnectReason reason;

    public PacketForceDisconnect() {
    }

    public PacketForceDisconnect(DisconnectReason reason) {
        this.reason = reason;
    }

    @Override
    public void send(WrappedOutputStream out) throws IOException {
        // Writes data
        out.writeInt(this.reason.getId());
    }

    @Override
    public void receive(WrappedInputStream in) throws IOException {
        // Reads data
        this.reason = DisconnectReason.getFromId(in.readInt());
    }

    public DisconnectReason getReason() {
        return reason;
    }
}