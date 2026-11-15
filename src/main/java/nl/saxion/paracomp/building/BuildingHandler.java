package nl.saxion.paracomp.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import nl.saxion.paracomp.Constants;
import nl.saxion.paracomp.domain.entities.Reservation;
import nl.saxion.paracomp.domain.entities.Room;
import nl.saxion.paracomp.domain.messages.ErrorMessage;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;
import nl.saxion.paracomp.domain.messages.building.RegisterBuildingMessage;
import nl.saxion.paracomp.domain.messages.reservation.CancelReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationRequestMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationResponseMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BuildingHandler {

    private static final Logger LOGGER = Logger.getLogger(BuildingHandler.class.getName());

    private static final long HEARTBEAT_INTERVAL_SECONDS = 5;

    private final String name;
    private final List<Room> rooms;
    private final Channel channel;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper mapper = new ObjectMapper();

    public BuildingHandler(String name, List<Room> rooms, Channel channel) {
        this.name = name;
        this.rooms = rooms;
        this.channel = channel;
    }

    public void startHeartbeat() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        try {
            channel.basicPublish(Constants.EXCHANGE_BUILDING_PRESENCE,
                                 "",
                                 null,
                                 mapper.writeValueAsBytes(new RegisterBuildingMessage(name,
                                                                                      Collections.unmodifiableList(rooms))));

            LOGGER.info("BuildingAgent " + name + " sent heartbeat.");
        } catch (Exception e) {
            LOGGER.severe("Failed to send heartbeat: " + e.getMessage());
        }
    }

    public void handleRequest(byte[] delivery, String replyQueue) {
        try {
            Message message = mapper.readValue(delivery, Message.class);

            switch (message) {
                case ReservationRequestMessage request -> tryReserve(request, replyQueue);
                case CancelReservationMessage request -> cancelReservation(request, replyQueue);
                default -> LOGGER.warning("Unexpected message: " + message.type);
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private synchronized void tryReserve(ReservationRequestMessage request, String replyQueue) {
        if (!isCorrectBuilding(request.buildingName, request, replyQueue)) {
            return;
        }

        Room room = getRoom(request.roomNumber);

        if (room != null && !room.isReserved()) {
            room.setReserved(true);

            final Reservation reservation = new Reservation(name, request.roomNumber);
            final ReservationResponseMessage response = new ReservationResponseMessage(reservation);

            response.clientReplyQueue = request.clientReplyQueue;
            sendResponse(response, replyQueue);
        } else {
            ErrorMessage error = new ErrorMessage("Room was already reserved");
            error.clientReplyQueue = request.clientReplyQueue;
            sendResponse(error, replyQueue);
        }
    }

    private synchronized void cancelReservation(CancelReservationMessage request, String replyQueue) {
        if (!isCorrectBuilding(request.buildingName, request, replyQueue)) {
            return;
        }

        Room room = getRoom(request.roomNumber);

        if (room == null || !room.isReserved()) {
            ErrorMessage error = new ErrorMessage("Room is not reserved");
            error.clientReplyQueue = request.clientReplyQueue;
            sendResponse(error, replyQueue);
            return;
        }

        room.setReserved(false);

        Message response = new Message(MessageType.RESERVATION_CANCELED);
        response.clientReplyQueue = request.clientReplyQueue;
        sendResponse(response, replyQueue);
    }

    private boolean isCorrectBuilding(String buildingName, Message request, String replyQueue) {
        if (!buildingName.equals(this.name)) {
            LOGGER.warning("Wrong building: expected '" + this.name
                                   + "', received '" + buildingName + "'");

            ErrorMessage error = new ErrorMessage("Message received by the wrong building");
            error.clientReplyQueue = request.clientReplyQueue;

            sendResponse(error, replyQueue);

            return false;
        }
        return true;
    }

    private Room getRoom(int roomNumber) {
        return rooms.stream()
                    .filter(room -> room.getNumber() == roomNumber)
                    .findFirst()
                    .orElse(null);
    }

    private void sendResponse(Message message, String replyQueue) {
        try {
            channel.basicPublish(Constants.EXCHANGE_RENTAL_REPLIES,
                                 replyQueue,
                                 null,
                                 mapper.writeValueAsBytes(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        scheduler.shutdown();
    }
}
