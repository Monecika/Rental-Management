package nl.saxion.paracomp.domain.messages.building;

import nl.saxion.paracomp.domain.entities.Room;
import nl.saxion.paracomp.domain.messages.Message;
import nl.saxion.paracomp.domain.messages.MessageType;

import java.util.List;

public class RegisterBuildingMessage extends Message {
    public String buildingName;
    public List<Room> rooms;

    public RegisterBuildingMessage() {
        super(MessageType.REGISTER_BUILDING);
    }


    public RegisterBuildingMessage(String buildingName, List<Room> rooms) {
        this();

        this.buildingName = buildingName;
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return "RegisterBuildingMessage{" + "buildingName='" + buildingName + '\'' + ", rooms=" + rooms + '}';
    }
}