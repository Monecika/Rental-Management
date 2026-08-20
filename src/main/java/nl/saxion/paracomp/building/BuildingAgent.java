package nl.saxion.paracomp.building;

import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Utils;

import java.util.Scanner;
import java.util.logging.Logger;

public class BuildingAgent implements AutoCloseable{

    private final static Logger LOGGER = Logger.getLogger(BuildingAgent.class.getName());

    private final String name;
    private Connection connection;

    static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "building1";
        Scanner scanner = new Scanner(System.in);

        try(var buildingAgent = new BuildingAgent(name)){
            LOGGER.info("BuildingAgent '" + name + "' is running. Press Enter to exit...");

            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BuildingAgent() {
        this("building1");
    }

    public BuildingAgent(String name) {
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
