package nl.saxion.paracomp.domain.messages;

public enum MessageType {
    LIST_BUILDINGS,
    BUILDING_LIST,

    RESERVE_BUILDING,
    RESERVATION_RESPONSE,
    CONFIRM_RESERVATION,
    CANCEL_RESERVATION,

    RESERVATION_CONFIRMED,
    RESERVATION_CANCELED,

    REGISTER_BUILDING,

    ERROR;

    public String routingKey() {
        return name().toLowerCase();
    }
}