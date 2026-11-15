package nl.saxion.paracomp.domain.messages.reservation;

import nl.saxion.paracomp.domain.entities.Reservation;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

public class ReservationResponseMessage extends Message {
    public Reservation reservation;

    public ReservationResponseMessage() {
        super(MessageType.RESERVATION_RESPONSE);
    }

    public ReservationResponseMessage(Reservation reservation) {
        this();
        this.reservation = reservation;
    }

    @Override
    public String toString() {
        return "ReservationResponseMessage{" +
                "reservation=" + reservation +
                '}';
    }
}