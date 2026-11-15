package nl.saxion.paracomp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import nl.saxion.paracomp.Constants;
import nl.saxion.paracomp.Utils;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;
import nl.saxion.paracomp.domain.messages.building.BuildingListMessage;
import nl.saxion.paracomp.domain.messages.reservation.CancelReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ConfirmReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationRequestMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationResponseMessage;

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientAgent implements Closeable {
    private static final long RESPONSE_TIMEOUT_SECONDS = 5;

    private final String name;
    private final ObjectMapper mapper = new ObjectMapper();

    private Connection connection;
    private Channel channel;
    private String replyQueue;
    private CountDownLatch latch;

    public ClientAgent() {
        this("client1");
    }

    public ClientAgent(String name) {
        this.name = name;
        setupRabbitMQ();
    }

    public String getName() {
        return name;
    }

    private void setupRabbitMQ() {
        try {
            connection = Utils.createConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(Constants.EXCHANGE_RENTAL_DIRECT, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(Constants.EXCHANGE_CLIENT_REPLIES, BuiltinExchangeType.DIRECT);

            replyQueue = channel.queueDeclare()
                                .getQueue();

            channel.queueBind(replyQueue, Constants.EXCHANGE_CLIENT_REPLIES, replyQueue);

            channel.basicConsume(replyQueue, true, (_, delivery) -> handleReply(delivery.getBody()), _ -> {
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void requestBuildingList() {
        send(new Message(MessageType.LIST_BUILDINGS));
    }

    public void requestReservation(String buildingName, int roomNumber) {
        send(new ReservationRequestMessage(buildingName, roomNumber));
    }

    public void confirmReservation(UUID reservationId) {
        send(new ConfirmReservationMessage(reservationId));
    }

    public void cancelReservation(UUID reservationId) {
        send(new CancelReservationMessage(reservationId));
    }

    private void send(Message request) {
        try {
            latch = new CountDownLatch(1);

            BasicProperties properties = new BasicProperties.Builder().replyTo(replyQueue)
                                                                      .build();

            channel.basicPublish(Constants.EXCHANGE_RENTAL_DIRECT,
                                 request.type.routingKey(),
                                 properties,
                                 mapper.writeValueAsBytes(request));

            if (!latch.await(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Request timed out: " + request.type);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void handleReply(byte[] body) {
        try {
            Message message = mapper.readValue(body, Message.class);

            switch (message) {
                case BuildingListMessage response -> printResponse(response);
                case ReservationResponseMessage response -> printResponse(response);
                default -> printResponse(message);
            }

        } catch (Exception e) {
            System.err.println("Could not process reply: " + e.getMessage());

        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    private void printResponse(Message response) {
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter()
                                     .writeValueAsString(response));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }

            if (connection != null && connection.isOpen()) {
                connection.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}