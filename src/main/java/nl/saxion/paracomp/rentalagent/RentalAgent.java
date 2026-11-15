package nl.saxion.paracomp.rentalagent;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Constants;
import nl.saxion.paracomp.Utils;
import nl.saxion.paracomp.domain.messages.MessageType;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

public class RentalAgent implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(RentalAgent.class.getName());

    private final String name;
    private Connection connection;
    private Channel channel;
    private RentalHandler handler;
    private String rentalReplyQueue;

    public RentalAgent() {
        this("rentalagent1");
    }

    public RentalAgent(String name) {
        this.name = name;
        setupRabbitMQ();
    }

    static void main(String[] args) {
        String name = args.length > 0 ?
                args[0] :
                "rentalagent1";

        Scanner scanner = new Scanner(System.in);

        try (var rentalAgent = new RentalAgent(name)) {
            LOGGER.info("RentalAgent '" + name + "' is running. Press Enter to exit...");

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
            channel.exchangeDeclare(Constants.EXCHANGE_RENTAL_DIRECT, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(Constants.EXCHANGE_CLIENT_REPLIES, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(Constants.EXCHANGE_RENTAL_REPLIES, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(Constants.EXCHANGE_BUILDING_DIRECT, BuiltinExchangeType.DIRECT);

            rentalReplyQueue = channel.queueDeclare()
                                      .getQueue();
            channel.queueBind(rentalReplyQueue, Constants.EXCHANGE_RENTAL_REPLIES, rentalReplyQueue);

            handler = new RentalHandler(name, channel, rentalReplyQueue);

            setupBuildingSide();
            setupClientSide();
            setupBuildingReplies();

            handler.startStaleBuildingMonitor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupBuildingSide() throws IOException {
        String presenceQueue = channel.queueDeclare()
                                      .getQueue();

        channel.queueBind(presenceQueue, Constants.EXCHANGE_BUILDING_PRESENCE, "");

        channel.basicConsume(presenceQueue, true, (_, delivery) -> handler.handleHeartbeat(delivery.getBody()), _ -> {
        });
    }

    private void setupBuildingReplies() throws IOException {
        channel.basicConsume(rentalReplyQueue, true, (_, delivery) -> {
            handler.handleBuildingResponse(delivery.getBody());
        }, _ -> {
        });
    }

    private void setupClientSide() throws IOException {
        channel.queueDeclare(Constants.QUEUE_RENTAL_REQUESTS, true, false, false, null);

        channel.queueBind(Constants.QUEUE_RENTAL_REQUESTS,
                          Constants.EXCHANGE_RENTAL_DIRECT,
                          MessageType.LIST_BUILDINGS.routingKey());

        channel.queueBind(Constants.QUEUE_RENTAL_REQUESTS,
                          Constants.EXCHANGE_RENTAL_DIRECT,
                          MessageType.RESERVE_BUILDING.routingKey());

        channel.queueBind(Constants.QUEUE_RENTAL_REQUESTS,
                          Constants.EXCHANGE_RENTAL_DIRECT,
                          MessageType.CONFIRM_RESERVATION.routingKey());

        channel.queueBind(Constants.QUEUE_RENTAL_REQUESTS,
                          Constants.EXCHANGE_RENTAL_DIRECT,
                          MessageType.CANCEL_RESERVATION.routingKey());

        channel.basicConsume(Constants.QUEUE_RENTAL_REQUESTS,
                             true,
                             (_, delivery) -> handler.handleClientRequest(delivery.getBody(),
                                                                          delivery.getProperties()
                                                                                  .getReplyTo()),
                             _ -> {
                             });
    }

    @Override
    public void close() throws Exception {
        handler.close();
        channel.close();
        connection.close();
    }
}