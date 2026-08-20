package nl.saxion.paracomp;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class Utils {
    private final static Logger LOGGER = Logger.getLogger(Utils.class.getName());

    // enable next lines to disable logging
//    static {
//        LOGGER.setLevel(java.util.logging.Level.OFF);
//    }

    public static Connection createConnection() throws IOException, TimeoutException {
        var factory = new ConnectionFactory();
        var props = new Properties();
        props.load(Utils.class.getResourceAsStream("rabbitmq.properties"));
        factory.load(props);
        LOGGER.info(String.format("Connecting to RabbitMQ server at %s:%d", factory.getHost(), factory.getPort()));
        var connection = factory.newConnection();
        LOGGER.info(String.format("Connected to RabbitMQ server as %s", factory.getUsername()));
        return connection;
    }
}
