import me.ImSpooks.nettycore.client.CoreClient;
import me.ImSpooks.nettycore.client.networking.IncomingListener;
import me.ImSpooks.nettycore.packets.collection.networking.PacketConfirmConnection;
import me.ImSpooks.nettycore.packets.collection.networking.PacketRequestConnection;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Start {

    public static void main(String[] args) {
        CoreClient client = CoreClient.main(args);

        client.addIncomingListener(PacketConfirmConnection.class, new IncomingListener<PacketConfirmConnection>(Long.MAX_VALUE) {
            @Override
            protected boolean onReceive(PacketConfirmConnection packet) {
                // Packet received
                return false;
            }

            @Override
            protected void onExpire() {
                // Packet not received in given time
            }
        });
        client.sendPacket(new PacketRequestConnection());
    }
}