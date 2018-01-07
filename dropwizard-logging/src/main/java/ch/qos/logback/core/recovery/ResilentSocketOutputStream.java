package ch.qos.logback.core.recovery;

import javax.net.SocketFactory;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Represents a resilent persistent connection via TCP as an {@link OutputStream}.
 * Automatically tries to reconnect to the server if it encounters errors during writing
 * data via a TCP connection.
 */
public class ResilentSocketOutputStream extends ResilientOutputStreamBase {

    private final String host;
    private final int port;
    private final int connectionTimeoutMs;
    private final SocketFactory socketFactory;

    /**
     * Creates a new stream based on the socket configuration.
     *
     * @param host                The host or an IP address of the server.
     * @param port                The port on the server which accepts TCP connections.
     * @param connectionTimeoutMs The timeout for establishing a new TCP connection.
     * @param socketFactory       The factory for customizing the client socket.
     */
    public ResilentSocketOutputStream(String host, int port, int connectionTimeoutMs,
                                      SocketFactory socketFactory) {
        this.host = host;
        this.port = port;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.socketFactory = socketFactory;
        try {
            this.os = openNewOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create a TCP connection to " + host + ":" + port, e);
        }
        this.presumedClean = true;
    }

    @Override
    String getDescription() {
        return "tcp [" + host + ":" + port + "]";
    }

    @Override
    OutputStream openNewOutputStream() throws IOException {
        final Socket socket = socketFactory.createSocket();
        // Prevent automatic closing of the connection during periods of inactivity.
        socket.setKeepAlive(true);
        // Important not to cache `InetAddress` in case the host moved to a new IP address.
        socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), connectionTimeoutMs);
        return socket.getOutputStream();
    }

    @Override
    public void flush() {
        // The ResilientOutputStreamBase#flash method considers a successful flush as an indicator of a working
        // connection. For a direct socket stream it's a no-op, and a call will succeed even if the connection
        // is broken. That means a broken connection will never be restored. Hence, we leave this method empty.
    }
}