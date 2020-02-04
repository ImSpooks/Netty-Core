# Netty Core
This is a server/client communication system using Netty.
It communicates with the use of packets.

<i>This project was made for self learning purposes.</i>

1. [Requirements](#requirements)
2. [Build and Test](#build-and-test)
3. [Using this project](#using-this-project)
    1. [Adding packet types](#adding-packet-types)
    2. [Creating packets](#creating-packets)
    3. [Handling packets](#handling-packets)
    4. [Launching the client/server](#launching-the-clientserver)


## Requirements
- Java <i>(Tested in Java 11)</i>
- Maven

## Build and Test
Building this project is pretty easy, dependencies will automatically be downloaded using maven:
1. Clone this repository with `git clone https://github.com/ImSpooks/Netty-Core.git`
2. Browse to the cloned repository.
3. Run the command `mvn package` in terminal or build with maven using your IDE. The jar files will be build in the `out/` folder.

To run the server, go to the `out` folder and type `java -jar Server-1.0.jar`

To run the server, go to the `out` folder and type `java -jar Client-1.0.jar`

# Using this project
To use/edit this project all you need is a Java IDE such as IntelliJ with Maven.
With that IDE, you can open this project's pom and Maven will do the rest.

### Adding packet types
Creating packet types is fairly easy, this is all you have to do:
```java
class PacketRegisterer {

    public static void main(String[] args) {
        // Register packet type
        PacketType.registerPacketType("Example");
        
        // Get packet type
        PacketType.getPacketType("Example");
    }
}
```
##
### Creating packets
To create packets <i>(<b>Note:</b> `PacketIn` is used for client-to-server packets, `PacketOut` is used for server-to-client packets)</i>:
1. Create a class that extends either `Packet`, `PacketIn` or `PacketOut`.
2. Create variable fields and a constructor with these parameters and an empty constructor.
3. Write all your code in the `send` and `receive` method.

Example:

```java
public class PacketInExample extends PacketInt {
    private UUID uuid;
    private long time;
    
    // empty constructor
    public PacketInExample() {}
    
    public PacketInExample(UUID uuid) {
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
```

To register the packet in the system:
```java
// Registering packet
class PacketRegisterer {

    public static void main(String[] args) {
        // Register packet
        PacketRegister.register(PacketType.getPacketType("Example"), PacketInExample.class);

        // Sending packet from client
        core_instance.sendPacket(new PacketInExample(UUID.randomUUID()));

        // Sending packet from server
        client_serverpackethandler_instance.sendPacket(new PacketInExample(UUID.randomUUID()));
    }
}
```
##
### Handling packets
To handle packets you must have a class that extends to a sub packet handler:
1. Create a class that extends to SubPacketHandler
2. Every packet handling method must have the `@PacketHandling` annotation.
3. Every packet handling method must have `ChannelHandlerContext` as their first parameter type, and the second parameter as the packet

Example:

```java
public class ExamplePacketHandler extends SubPacketHandler {

    public NetworkPacketHandler(PacketHandler packetHandler, ClientSettings settings) {
        super(packetHandler, settings);
    }

    @PacketHandling
    public void handlePacket(ChannelHandlerContext ctx, PacketInExample packet) {
        System.out.println("Uuid: " + packet.getUuid().toString() + ", latency: " + (System.currentTimeMillis() - packet.getTime()) + " ms");
    }

}
```
Example to register the packet handler:
```java
class PacketRegisterer {

    public static void main(String[] args) {
        // Register packet handler
        PacketHandler.addPacketHandler(PacketType.getPacketType("Example"), ExamplePacketHandler.class);
    }
}
```
##
### Launching the client/server
To launch the client/server you need to do these few things:
```java
class Launcher {

    public static void main(String[] args) {
        /** Registering custom packets */

        PacketType.registerPacketType("Example");
        PacketRegister.register(PacketType.getPacketType("Example"), PacketInExample.class);
        PacketHandler.addPacketHandler(PacketType.getPacketType("Example"), ExamplePacketHandler.class);


        /** Launching */

        // Launch client with
        CoreClient.startClient();
        // Or launch the server with
        Core.startServer();
    }
}
```