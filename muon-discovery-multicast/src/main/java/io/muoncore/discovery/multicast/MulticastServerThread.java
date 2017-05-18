package io.muoncore.discovery.multicast;

import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.json.GsonCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastServerThread extends Thread {

    private long FIVE_SECONDS = 5000;

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;

    private InstanceDescriptor descriptor;
    private GsonCodec gson;
    private boolean running = true;

    public MulticastServerThread(InstanceDescriptor descriptor) throws IOException {
        super("MulticastServerThread");
        this.descriptor = descriptor;
        socket = new MulticastSocket(4445);
        this.gson = new GsonCodec();
    }

    public void run() {
        while (running) {
            try {
                byte[] buf = new byte[256];

                buf = gson.encode(descriptor);

                // send it
                InetAddress group = InetAddress.getByName(MulticastDiscovery.MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MulticastDiscovery.PORT);
                if (socket.isBound()) socket.send(packet);

                // sleep for a while
                try {
                    sleep((long)(Math.random() * FIVE_SECONDS));
                } catch (InterruptedException e) { }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    sleep((long)(Math.random() * FIVE_SECONDS));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        socket.close();
    }


    public void shutdown() {
        running = false;
    }
}
