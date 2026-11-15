package nl.saxion.paracomp.rentalagent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import nl.saxion.paracomp.Constants;
import nl.saxion.paracomp.domain.entities.Building;
import nl.saxion.paracomp.domain.entities.Reservation;
import nl.saxion.paracomp.domain.entities.ReservationStatus;
import nl.saxion.paracomp.domain.messages.ErrorMessage;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;
import nl.saxion.paracomp.domain.messages.building.BuildingListMessage;
import nl.saxion.paracomp.domain.messages.building.RegisterBuildingMessage;
import nl.saxion.paracomp.domain.messages.reservation.CancelReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ConfirmReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationRequestMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationResponseMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RentalHandler {
    private static final Logger LOGGER = Logger.getLogger(RentalHandler.class.getName());

    private static final long HEARTBEAT_TIMEOUT_MS = 15000;

    private final String name;
    private final Channel channel;
    private final String rentalReplyQueue;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Set<Building> buildings = new HashSet<>();
    private final Map<UUID, Reservation> reservations = new HashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public RentalHandler(String name, Channel channel, String rentalReplyQueue) {
        this.name = name;
        this.channel = channel;
        this.rentalReplyQueue = rentalReplyQueue;
    }

    public void handleClientRequest(byte[] delivery, String replyQueue) {
        try {
            Message message = mapper.readValue(delivery, Message.class);

            switch (message) {
                case BuildingListMessage _ -> {
                    message.clientReplyQueue = replyQueue;
                    handleListBuildings(message.clientReplyQueue);
                }
                case ReservationRequestMessage request -> {
                    request.clientReplyQueue = replyQueue;
                    handleReservationRequest(request);
                }
                case ConfirmReservationMessage request -> {
                    request.clientReplyQueue = replyQueue;
                    handleConfirmReservationRequest(request);
                }
                case CancelReservationMessage request -> {
                    request.clientReplyQueue = replyQueue;
                    handleCancelReservationRequest(request);
                }
                default -> {
                    message.clientReplyQueue = replyQueue;
                    sendResponse(message);
                }
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public void handleBuildingResponse(byte[] delivery) {
        try {
            Message message = mapper.readValue(delivery, Message.class);

            switch (message) {
                case ReservationResponseMessage response -> handleReservationResponse(response);
                default -> sendResponse(message);
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private void handleListBuildings(String replyQueue) {
        final BuildingListMessage message = new BuildingListMessage(getBuildings());
        message.clientReplyQueue = replyQueue;

        sendResponse(message);
    }

    private void handleReservationRequest(ReservationRequestMessage message) {
        LOGGER.info("RentalAgent " + name + " received reservation request: " + message);

        try {
            sendBuildingRequest(message, message.buildingName);
        } catch (Exception e) {
            LOGGER.severe("Failed to send reservation request: " + e.getMessage());

            final ErrorMessage error = new ErrorMessage("Failed to send reservation request");
            error.clientReplyQueue = message.clientReplyQueue;

            sendResponse(error);
        }
    }

    private void handleReservationResponse(ReservationResponseMessage response) {
        Reservation reservation = response.reservation;

        reservations.put(reservation.getId(), reservation);
        sendResponse(response);
    }

    private void handleConfirmReservationRequest(ConfirmReservationMessage message) {
        final Reservation reservation = reservations.get(message.reservationId);

        if (reservation != null) {
            reservation.setStatus(ReservationStatus.CONFIRMED);

            final Message response = new Message(MessageType.RESERVATION_CONFIRMED);
            response.clientReplyQueue = message.clientReplyQueue;

            sendResponse(response);
        } else {
            final ErrorMessage error = new ErrorMessage("Reservation not found");
            error.clientReplyQueue = message.clientReplyQueue;

            sendResponse(error);
        }
    }

    private void handleCancelReservationRequest(CancelReservationMessage message) {
        try {
            Reservation reservation = reservations.get(message.reservationId);

            if (reservation == null) {
                final ErrorMessage error = new ErrorMessage("Reservation not found");
                error.clientReplyQueue = message.clientReplyQueue;

                sendResponse(error);
                return;
            }

            message.setBuildingName(reservation.getBuildingName());
            message.setRoomNumber(reservation.getRoomNumber());

            reservations.remove(message.reservationId);
            sendBuildingRequest(message, reservation.getBuildingName());

        } catch (Exception e) {
            LOGGER.severe("Failed to send cancellation request: " + e.getMessage());

            final ErrorMessage error = new ErrorMessage("Failed to process cancellation");
            error.clientReplyQueue = message.clientReplyQueue;

            sendResponse(error);
        }
    }

    private void sendBuildingRequest(Message message, String buildingName) throws Exception {
        BasicProperties properties = new BasicProperties.Builder().replyTo(rentalReplyQueue)
                                                                  .build();

        channel.basicPublish(Constants.EXCHANGE_BUILDING_DIRECT,
                             buildingName,
                             properties,
                             mapper.writeValueAsBytes(message));
    }

    private void sendResponse(Message message) {
        try {
            channel.basicPublish(Constants.EXCHANGE_CLIENT_REPLIES,
                                 message.clientReplyQueue,
                                 null,
                                 mapper.writeValueAsBytes(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleHeartbeat(byte[] body) {
        try {
            RegisterBuildingMessage message = mapper.readValue(body, RegisterBuildingMessage.class);

            Building building = buildings.stream()
                                         .filter(b -> b.getName()
                                                       .equals(message.buildingName))
                                         .findFirst()
                                         .orElse(null);

            if (building == null) {
                Building newBuilding = new Building(message.buildingName, new ArrayList<>(message.rooms));

                buildings.add(newBuilding);

                LOGGER.info("RentalAgent " + name + " registered building: " + newBuilding.getName());
            } else {
                building.getRooms()
                        .clear();
                building.getRooms()
                        .addAll(message.rooms);
                building.markAlive();

                LOGGER.info("RentalAgent " + name + " updated building: " + building.getName());
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to process building heartbeat: " + e.getMessage());
        }
    }

    public void startStaleBuildingMonitor() {
        cleanupScheduler.scheduleAtFixedRate(this::removeStaleBuildings, 5, 5, TimeUnit.SECONDS);
    }

    private void removeStaleBuildings() {
        long now = System.currentTimeMillis();

        for (Building building : getBuildings()) {
            if (now - building.getLastHeartbeat() > HEARTBEAT_TIMEOUT_MS) {
                buildings.remove(building);
                LOGGER.warning("RentalAgent " + name + " removed stale building '" + building.getName() + "'.");
            }
        }
    }

    public void close() {
        cleanupScheduler.shutdown();
    }

    public List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }
}