package nl.saxion.paracomp.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Building {

    private final String name;
    private final List<Room> rooms = new ArrayList<>();
    private volatile long lastHeartbeat = System.currentTimeMillis();

    public Building(String name) {
        this.name = name;
    }

    @JsonCreator
    public Building(@JsonProperty("name") String name, @JsonProperty("rooms") List<Room> rooms) {
        this.name = name;
        if (rooms != null) {
            this.rooms.addAll(rooms);
        }
    }

    public String getName() {
        return name;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    @JsonIgnore
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void markAlive() {
        lastHeartbeat = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Building building = (Building) o;
        return Objects.equals(name, building.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "Building{" +
                "name='" + name + '\'' +
                ", rooms=" + rooms +
                '}';
    }
}