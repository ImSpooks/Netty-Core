package me.ImSpooks.nettycore.packets.handle;

import me.ImSpooks.nettycore.packets.handle.channels.WrappedInputStream;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedOutputStream;

import java.io.IOException;

/**
 * Packet instance
 */
public abstract class Packet {

    // Packet type
    private PacketType packetType;

    /**
     * Called when sending a packet
     *
     * @param out Wrapped output stream
     * @throws IOException if an IO error occurs
     */
    public abstract void send(WrappedOutputStream out) throws IOException;

    /**
     * Called when the packet is received
     *
     * @param in Wrapped input stream
     * @throws IOException if an IO error occurs
     */
    public abstract void receive(WrappedInputStream in) throws IOException;

    /**
     * @return Packet ID
     */
    public int getId() {
        return PacketRegister.getId(this);
    }

    /**
     * @return Packet type
     */
    public PacketType getPacketType() {
        return packetType != null ? packetType : (packetType = PacketRegister.getPacketType(this.getId()));
    }

    /**
     * Set packet type
     * @param packetType packetType
     */
    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }
}