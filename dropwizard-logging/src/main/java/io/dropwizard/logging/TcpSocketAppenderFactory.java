package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.logging.socket.DropwizardSocketAppender;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.PortRange;
import org.hibernate.validator.constraints.NotEmpty;

import javax.net.SocketFactory;
import javax.validation.constraints.NotNull;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a TCP socket.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code host}</td>
 * <td>{@code localhost}</td>
 * <td>The hostname of the TCP server.</td>
 * </tr>
 * <tr>
 * <td>{@code port}</td>
 * <td>{@code 4560}</td>
 * <td>The port on which the TCP server is listening.</td>
 * </tr>
 * <tr>
 * <td>{@code connectionTimeout}</td>
 * <td>{@code 500 ms}</td>
 * <td>The timeout to connect to the TCP server</td>
 * </tr>
 * </table>
 */
@JsonTypeName("tcp")
public class TcpSocketAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {

    @NotEmpty
    private String host = "localhost";

    @PortRange
    private int port = 4560;

    @NotNull
    private Duration connectionTimeout = Duration.milliseconds(500);

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    @JsonProperty
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public Appender<E> build(LoggerContext context, String applicationName, LayoutFactory<E> layoutFactory,
                             LevelFilterFactory<E> levelFilterFactory, AsyncAppenderFactory<E> asyncAppenderFactory) {
        final DropwizardSocketAppender<E> appender = new DropwizardSocketAppender<>(host, port,
            (int) connectionTimeout.toMilliseconds(), socketFactory());
        appender.setContext(context);
        appender.setName("tcp-socket-appender");

        final LayoutWrappingEncoder<E> layoutEncoder = new LayoutWrappingEncoder<>();
        layoutEncoder.setLayout(buildLayout(context, layoutFactory));
        appender.setEncoder(layoutEncoder);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();
        return wrapAsync(appender, asyncAppenderFactory);
    }

    private SocketFactory socketFactory() {
        return SocketFactory.getDefault();
    }

}
