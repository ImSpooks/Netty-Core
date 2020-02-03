# Netty Core
This is a server/client communication system using Netty.
It communicates with the use of packets.

<i>This project was made for self learning purposes.</i>


# Requirements
- Java <i>(Tested in Java 11)</i>
- Maven <i>(Build in with intellij)</i>

# Build and Test
Building this project is pretty easy, dependencies will automatically be downloaded using maven:
1. Clone this repository with `git clone https://github.com/ImSpooks/Netty-Core.git`
2. Browse to the cloned repository.
3. Run the command `mvn package` in terminal or build with maven using your IDE. The jar files will be build in the `out/` folder.

To run the server, go to the `out` folder and type `java -jar Server-1.0.jar`

To run the server, go to the `out` folder and type `java -jar Client-1.0.jar`

# How to use this project
To use/edit this project all you need is a Java IDE such as IntelliJ with Maven.
With that IDE, you can open this project's pom and Maven will do the rest.

### Adding packet types
To create a new packet type:
1. Go to the packets module and find the enum class `me.ImSpooks.nettycore.packets.PacketType.java`
2. Add a new packet type, e.g. `DATABASE(100; amount of current existing types * 100)`
##

### Creating packets
To create packets <i>(<b>Note:</b> `PacketIn` is used for client-to-server packets, `PacketOut` is used for server-to-client packets)</i>:

1. Find or create the package needed for your new packet in `me.ImSpooks.nettycore.packets.collection`
    - If the package does not exist for your type, create the package with packages `in` & `out` as child
2. Create a packet in the `in` or `out` that extends `PacketIn` or `PacketOut`, e.g. PacketInRequestConnection.
    - A response packet is helpful bot not needed, e.g. PacketOutConfirmConnection.
3. Create variable fields and a constructor with these parameters and an empty constructor
4. Write all variable data in the `send` method
5. Read all variable data in the `receive` method.
##

### Handling packets
To handle packets:

1. Find or create the packet handler class for a packet type in `me.ImSpooks.nettycore.<client and/or server>.packets.handle`, e.g. `NetworkPacketHandler`.
    - Extend the class to SubPacketHandler if not already.
    - Register the sub packet handler class in the constructor of the `PacketHandler` class.
2. Make a new method with parameters `ChannelHandlerContext` & `Packet to handle, e.g. PacketInRequestConnection`.
3. Add the `@PacketHandling` annotation to that method.
4. Write the necessary code to handle your packet.
##
