package nl.saxion.paracomp.domain.messages.reservation;

import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

public class ReservationRequestMessage extends Message {
    public String buildingName;
    public int roomNumber;

    public ReservationRequestMessage() {
        super(MessageType.RESERVE_BUILDING);
    }

    public ReservationRequestMessage(String buildingName, int roomNumber) {
        this();
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return "ReservationRequestMessage{" +
                "buildingName='" + buildingName + '\'' +
                ", roomNumber=" + roomNumber +
                '}';
    }
}