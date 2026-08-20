package nl.saxion.paracomp.rentalagent;

import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Utils;

import java.util.Scanner;
import java.util.logging.Logger;

public class RentalAgent implements AutoCloseable{

    private final static Logger LOGGER = Logger.getLogger(RentalAgent.class.getName());

    private final String name;
    private Connection connection;

    static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "rentalagent1";
        Scanner scanner = new Scanner(System.in);

        try(var rentalAgent = new RentalAgent(name)){
            LOGGER.info("RentalAgent '" + name + "' is running. Press Enter to exit...");

            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RentalAgent() {
        this("rentalagent1");
    }

    public RentalAgent(String name) {
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
    public void close() throws Exception{
        connection.close();
    }
}
