package nl.saxion.paracomp.domain.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.saxion.paracomp.domain.messages.building.BuildingListMessage;
import nl.saxion.paracomp.domain.messages.reservation.CancelReservationMessage;
import nl.saxion.paracomp.domain.messages.reservation.ConfirmReservationMessage;
import nl.saxion.paracomp.domain.messages.building.RegisterBuildingMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationRequestMessage;
import nl.saxion.paracomp.domain.messages.reservation.ReservationResponseMessage;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = Message.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuildingListMessage.class, name = "LIST_BUILDINGS"),
        @JsonSubTypes.Type(value = ReservationRequestMessage.class, name = "RESERVE_BUILDING"),
        @JsonSubTypes.Type(value = ReservationResponseMessage.class, name = "RESERVATION_RESPONSE"),
        @JsonSubTypes.Type(value = RegisterBuildingMessage.class, name = "REGISTER_BUILDING"),
        @JsonSubTypes.Type(value = ConfirmReservationMessage.class, name = "CONFIRM_RESERVATION"),
        @JsonSubTypes.Type(value = CancelReservationMessage.class, name = "CANCEL_RESERVATION"),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = "ERROR")
})
public class Message {
    public MessageType type;
    public String clientReplyQueue;

    public Message() {
    }

    public Message(MessageType type) {
        this.type = type;
    }
}