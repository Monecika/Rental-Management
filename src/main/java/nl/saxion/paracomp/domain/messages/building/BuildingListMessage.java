package nl.saxion.paracomp.domain.messages.building;

import nl.saxion.paracomp.domain.entities.Building;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

import java.util.List;

public class BuildingListMessage extends Message {
    public List<Building> buildings;

    public BuildingListMessage() {
        super(MessageType.LIST_BUILDINGS);
    }

    public BuildingListMessage(List<Building> buildings) {
        this();
        this.buildings = buildings;
    }

    @Override
    public String toString() {
        return "BuildingListMessage{" +
                "buildings=" + buildings +
                '}';
    }
}