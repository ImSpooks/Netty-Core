package me.ImSpooks.nettycore.packets.handle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketType {

    private final String name;

    /**
     * Packet type constructor
     *
     * @param name Packet type name
     */
    public PacketType(String name) {
        this.name = name;
    }

    /**
     * @return Packet type name
     */
    public String getName() {
        return name;
    }

    // Pre defined packet types
    /**
     * Networking
     */
    public static final PacketType NETWORKING = new PacketType("NETWORKING");


    private static List<PacketType> packetTypes = new ArrayList<>();

    static {
        // Add pre defined packet types;
        packetTypes.add(NETWORKING);
    }

    /**
     * Register a packet type
     *
     * @param name Packet type name
     * @see PacketType#registerPacketType(PacketType) 
     */
    public static void registerPacketType(String name) {
        registerPacketType(new PacketType(name));
    }

    /**
     * Register a packet type
     *
     * @param type Packet type
     */
    public static void registerPacketType(PacketType type) {
        for (PacketType packetType : packetTypes) {
            if (packetType.getName().equalsIgnoreCase(type.getName())) {
                throw new IllegalArgumentException("Packet type with name \"" + type.getName() + "\" already exists.");
            }
            ;        }
        packetTypes.add(type);
    }

    /**
     * Returns a packet type with given name
     *
     * @param name Packet type name
     * @return Packet type with given name
     */
    public static PacketType getPacketType(String name) {
        for (PacketType packetType : packetTypes) {
            if (packetType.getName().equalsIgnoreCase(name)) {
                return packetType;
            }
        }
        throw new IllegalArgumentException("No packet type found with the name \"" + name + "\"");
    }

    /**
     * @return All registered packet types
     */
    public static List<PacketType> getPacketTypes() {
        return packetTypes;
    }
}