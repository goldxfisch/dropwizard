package ch.qos.logback.core.recovery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ResilentUdpSocketOutputStreamTest {

    private ResilentUdpSocketOutputStream udpOutputStream;
    private DatagramSocket datagramSocket;
    private Thread thread;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        datagramSocket = new DatagramSocket();
        thread = new Thread(() -> {
            byte[] buffer = new byte[128];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    assertThat(new String(buffer, 0, datagramPacket.getLength(), UTF_8))
                        .isEqualTo("Test message");
                    countDownLatch.countDown();
                } catch (IOException e) {
                    break;
                }
            }
        });
        thread.start();
        udpOutputStream = new ResilentUdpSocketOutputStream("localhost", datagramSocket.getLocalPort());
    }

    @After
    public void tearDown() throws Exception {
        datagramSocket.close();
        thread.interrupt();
        udpOutputStream.close();
    }

    @Test
    public void testCreateOutputStream() {
        assertThat(udpOutputStream.presumedClean).isTrue();
        assertThat(udpOutputStream.os).isNotNull();
    }

    @Test
    public void testSendMessage() throws Exception {
        assertThat(udpOutputStream.presumedClean).isTrue();
        assertThat(udpOutputStream.os).isNotNull();

        udpOutputStream.write("Test message".getBytes(UTF_8));
        udpOutputStream.flush();

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testGetDescription() {
        assertThat(udpOutputStream.getDescription()).isEqualTo(String.format("udp [localhost:%d]",
            datagramSocket.getLocalPort()));
    }
}
