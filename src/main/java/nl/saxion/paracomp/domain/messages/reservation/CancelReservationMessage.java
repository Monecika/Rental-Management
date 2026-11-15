package nl.saxion.paracomp.domain.messages.reservation;

import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

import java.util.UUID;

public class CancelReservationMessage extends Message {

    public UUID reservationId;
    public String buildingName;
    public int roomNumber;

    public CancelReservationMessage() {
        super(MessageType.CANCEL_RESERVATION);
    }

    public CancelReservationMessage(UUID reservationId) {
        this();
        this.reservationId = reservationId;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return "CancelReservationMessage{" + "reservationId=" + reservationId + ", buildingName='" + buildingName + '\'' + ", roomNumber=" + roomNumber + '}';
    }
}
