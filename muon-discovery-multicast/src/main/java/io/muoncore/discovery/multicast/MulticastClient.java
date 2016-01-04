package io.muoncore.discovery.multicast;

import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.json.GsonCodec;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class MulticastClient extends Thread {

    private boolean running = true;
    private ServiceCache serviceCache;
    private GsonCodec codec;

    public MulticastClient(ServiceCache serviceCache) {
        super("Muon Multicast Discovery Client");
        this.serviceCache = serviceCache;
        this.codec = new GsonCodec();
    }

    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(4446);
            InetAddress address = InetAddress.getByName("230.0.0.1");

            socket.joinGroup(address);

            DatagramPacket packet;

            // get a few quotes
            while(running) {

                byte[] buf = new byte[512];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                byte[] data = Arrays.copyOfRange(buf, 0, packet.getLength());

                System.out.println("Adding new stuff :" + data.length);
                ServiceDescriptor descriptor = codec.decode(data, ServiceDescriptor.class);
                System.out.println("Adding new service! " + descriptor.getIdentifier());
                serviceCache.addService(descriptor);
            }

            socket.leaveGroup(address);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        running = false;
    }
}