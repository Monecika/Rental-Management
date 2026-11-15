package nl.saxion.paracomp.building;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Constants;
import nl.saxion.paracomp.Utils;
import nl.saxion.paracomp.domain.entities.Room;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class BuildingAgent implements AutoCloseable {

    private final static Logger LOGGER = Logger.getLogger(BuildingAgent.class.getName());

    private final String name;
    private final List<Room> rooms;

    private Connection connection;
    private Channel channel;
    private BuildingHandler handler;

    public BuildingAgent() {
        this("building2");
    }

    public BuildingAgent(String name) {
        this.name = name;
        this.rooms = List.of(new Room(1), new Room(2), new Room(3), new Room(4), new Room(5));

        setupRabbitMQ();
    }

    static void main(String[] args) {
        String name = args.length > 0 ?
                args[0] :
                "building2";

        Scanner scanner = new Scanner(System.in);

        try (var buildingAgent = new BuildingAgent(name)) {
            LOGGER.info("BuildingAgent '" + name + "' is running. Press Enter to exit...");

            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    private void setupRabbitMQ() {
        try {
            connection = Utils.createConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(Constants.EXCHANGE_BUILDING_PRESENCE, BuiltinExchangeType.FANOUT);
            channel.exchangeDeclare(Constants.EXCHANGE_BUILDING_DIRECT, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(Constants.EXCHANGE_RENTAL_REPLIES, BuiltinExchangeType.DIRECT);

            handler = new BuildingHandler(name, rooms, channel);

            String requestQueue = channel.queueDeclare()
                                         .getQueue();

            channel.queueBind(requestQueue, Constants.EXCHANGE_BUILDING_DIRECT, name);

            channel.basicConsume(requestQueue,
                                 true,
                                 (_, delivery) -> handler.handleRequest(delivery.getBody(),
                                                                        delivery.getProperties()
                                                                                .getReplyTo()),
                                 _ -> {
                                 });

            handler.startHeartbeat();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        handler.close();
        channel.close();
        connection.close();
    }
}