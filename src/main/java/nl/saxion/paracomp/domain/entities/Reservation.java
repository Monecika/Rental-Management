package nl.saxion.paracomp.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Reservation {

    private final UUID id;
    private final String buildingName;
    private final int roomNumber;
    private ReservationStatus status;

    @JsonCreator
    public Reservation(
            @JsonProperty("id") UUID id,
            @JsonProperty("buildingName") String buildingName,
            @JsonProperty("roomNumber") int roomNumber,
            @JsonProperty("status") ReservationStatus status) {
        this.id = id;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.status = status;
    }

    public Reservation(String buildingName, int roomNumber) {
        this(UUID.randomUUID(), buildingName, roomNumber, ReservationStatus.PENDING);
    }

    public UUID getId() {
        return id;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Reservation{" + "id=" + id + ", buildingName='" + buildingName + '\'' + ", roomNumber=" + roomNumber + ", status=" + status + '}';
    }
}