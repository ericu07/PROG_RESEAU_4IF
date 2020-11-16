package tp.server;

import java.util.LinkedList;

public class Room {

    Integer roomId;

    LinkedList<String> messagesHistoric = new LinkedList<>();

    LinkedList<Client> members = new LinkedList<>();

    Room(Integer id) {
        roomId = id;
    }

}
