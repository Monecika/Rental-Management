package nl.saxion.paracomp.client;

import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Utils;

import java.io.Closeable;
import java.io.IOException;

public class ClientAgent implements Closeable {

    private final String name;
    private Connection connection;

    public ClientAgent(){
        this("client1");
    }

    public ClientAgent(String name) {
        this.name = name;
        setupRabbitMQ();
        // TODO
    }

    public String getName() {
        return name;
    }

    private void setupRabbitMQ() {
        try {
            connection = Utils.createConnection();
            // TODO
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
