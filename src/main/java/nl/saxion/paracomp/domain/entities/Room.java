package nl.saxion.paracomp.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Room {

    private final int number;
    private boolean reserved;

    @JsonCreator
    public Room(
            @JsonProperty("number") int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        return "Room{" + "number=" + number + ", reserved=" + reserved + '}';
    }
}