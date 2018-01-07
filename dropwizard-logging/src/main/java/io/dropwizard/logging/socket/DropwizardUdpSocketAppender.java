package io.dropwizard.logging.socket;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.recovery.ResilentUdpSocketOutputStream;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import java.io.OutputStream;

/**
 * Sends log events to a UDP server, a connection to which is represented as {@link ResilentUdpSocketOutputStream}.
 */
public class DropwizardUdpSocketAppender<E extends DeferredProcessingAware> extends OutputStreamAppender<E> {

    private final String host;
    private final int port;

    public DropwizardUdpSocketAppender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {
        setOutputStream(socketOutputStream());
        super.start();
    }

    protected OutputStream socketOutputStream() {
        final ResilentUdpSocketOutputStream outputStream = new ResilentUdpSocketOutputStream(host, port);
        outputStream.setContext(context);
        return outputStream;
    }
}
