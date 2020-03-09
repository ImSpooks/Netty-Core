package me.ImSpooks.nettycore.packets.handle;

import me.ImSpooks.nettycore.packets.collection.networking.PacketConfirmConnection;
import me.ImSpooks.nettycore.packets.collection.networking.PacketForceDisconnect;
import me.ImSpooks.nettycore.packets.collection.networking.PacketRequestConnection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick on 01 okt. 2019.
 * Copyright © ImSpooks
 */
public class PacketRegister {

    // Variables
    private static final Map<Integer, Class<? extends Packet>> REGISTERED_PACKETS = new HashMap<>();
    private static final Map<Class<? extends Packet>, Integer> REGISTERED_IDS = new HashMap<>();
    private static final Map<Integer, PacketType> PACKET_TYPES = new HashMap<>();

    static {
        // Initializes all packets
        register(PacketType.NETWORKING, PacketRequestConnection.class);
        register(PacketType.NETWORKING, PacketConfirmConnection.class);
        register(PacketType.NETWORKING, PacketForceDisconnect.class);
    }

    /**
     * Register a packet
     *
     * @param packetType Packet type
     * @param packet Packet class
     */
    public static void register(PacketType packetType, Class<? extends Packet> packet) {
        int id = REGISTERED_PACKETS.size();

        if (REGISTERED_IDS.containsKey(packet)) {
            throw new IllegalArgumentException("Packet " + packet + " already registered");
        }

        REGISTERED_PACKETS.put(id, packet);
        REGISTERED_IDS.put(packet, id);
        PACKET_TYPES.put(id, packetType);
    }

    /**
     * Creates a packet using a packet id with reflection.
     *
     * @param id Packet id
     * @return Packet instance
     */
    public static Packet createInstance(int id) {
        if (id < 0)
            throw new IllegalArgumentException("Illegal id range " + id);

        try {
            Class<? extends Packet> p = REGISTERED_PACKETS.get(id);
            if (p == null)
                throw new IllegalArgumentException("Unknown packet ID " + id);

            Constructor<? extends Packet> s = p.getDeclaredConstructor();
            s.setAccessible(true);
            return s.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param packet Packet instance
     * @return Packet id
     */
    public static int getId(Packet packet) {
        if (packet == null)
            throw new IllegalArgumentException("Packet may not be null");

        Integer id = REGISTERED_IDS.get(packet.getClass());
        if (id == null)
            throw new IllegalArgumentException("Unknown packet ID " + packet.getClass().getName());
        if (id < 0)
            throw new AssertionError("Packet id had impossible value " + id);

        return id;
    }

    /**
     * @param id Packet id
     * @return Packet type
     */
    public static PacketType getPacketType(int id) {
        if (id < 0 && !String.valueOf(id).startsWith("-"))
            throw new IllegalArgumentException("Illegal id range " + id);
        if (!PACKET_TYPES.containsKey(id))
            throw new IllegalArgumentException("Invalid packet id " + id);
        return PACKET_TYPES.get(id);
    }

    /**
     * @param id Packet id
     * @return Packet name
     */
    public static String getPacketName(int id) {
        if (id < 0&& !String.valueOf(id).startsWith("-"))
            throw new IllegalArgumentException("Illegal id range " + id);
        if (!REGISTERED_PACKETS.containsKey(id))
            throw new IllegalArgumentException("Invalid packet id " + id);

        return REGISTERED_PACKETS.get(id).getSimpleName();
    }

    /**
     * @param className Packet class name
     * @return Packet class
     * @throws ClassNotFoundException if class could not be found
     */
    public static Class<? extends Packet> getPacketFromClassName(String className) throws ClassNotFoundException {
        for (Class<? extends Packet> packet : getPackets()) {
            if (packet.getSimpleName().equalsIgnoreCase(className)) {
                return packet;
            }
        }
        throw new ClassNotFoundException("No registered packets with name \"" + className + "\" found");
    }

    /**
     * @return List of packets
     */
    public static Collection<Class<? extends Packet>> getPackets() {
        return new ArrayList<>(REGISTERED_IDS.keySet());
    }
}