package ch.qos.logback.core.recovery;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Represents a resilent UDP socket as an {@link OutputStream}. If the client can't send
 * data to the server, it automatically tries to reconnect to it. Of course UDP is not
 * reliable by definition, by this implementation allows to us to handle sporadic network
 * hiccups and DNS issues.
 */
public class ResilentUdpSocketOutputStream extends ResilientOutputStreamBase {

    private final String host;
    private final int port;

    public ResilentUdpSocketOutputStream(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.os = openNewOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create an UDP connection to " + host + ":" + port, e);
        }
        this.presumedClean = true;
    }

    @Override
    String getDescription() {
        return "udp [" + host + ":" + port + "]";
    }

    @Override
    OutputStream openNewOutputStream() throws IOException {
        return new DatagramSocketStream(host, port);
    }

    private static class DatagramSocketStream extends OutputStream {

        private final InetAddress address;
        private final int port;
        private final DatagramSocket datagramSocket;

        private DatagramSocketStream(String host, int port) throws IOException {
            // Send a new DNS request in case the host moved to a new IP address.
            address = InetAddress.getByName(host);
            datagramSocket = new DatagramSocket();
            this.port = port;
        }

        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("Datagram doesn't work at byte level");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            datagramSocket.send(new DatagramPacket(b, off, len, address, port));
        }

        @Override
        public void close() throws IOException {
            datagramSocket.close();
        }
    }
}
