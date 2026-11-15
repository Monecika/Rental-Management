package nl.saxion.paracomp.domain.messages.reservation;

import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

import java.util.UUID;

public class ConfirmReservationMessage extends Message {
    public UUID reservationId;

    public ConfirmReservationMessage() {
        super(MessageType.CONFIRM_RESERVATION);
    }


    public ConfirmReservationMessage(UUID reservationId) {
        this();

        this.reservationId = reservationId;
    }

    @Override
    public String toString() {
        return "ConfirmReservationMessage{" + "reservationId=" + reservationId + '}';
    }
}