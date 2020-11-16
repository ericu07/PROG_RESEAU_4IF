package tp.TCP.server;

import java.util.LinkedList;

public class Room {

    // Attributs

    protected Integer roomId;
    protected String roomName = null;

    protected LinkedList<String> messagesHistoric = new LinkedList<>();

    protected LinkedList<Client> members = new LinkedList<>();

    // Constructeurs

    public Room(Integer id) {
        roomId = id;
        broadcast("Room " + id + " created");
    }

    public Room(Integer id, String name) {
        roomId = id;
        roomName = name;
        broadcast("Room " + id + " : '" + name + "' created");
    }

    // Méthodes

    public String toString() {
        if (roomName == null) {
            return "Room " + roomId + " (" + members.size() + " users)";
        } else {
            return "Room " + roomId + " : " + roomName  + " (" + members.size() + " users)";
        }
    }

    public void broadcast(String message) {
        messagesHistoric.addLast(message);
        for (Client c : members) {
            c.send(message);
        }
    }

    public String historic() {
        StringBuilder historic = new StringBuilder("Room '" + roomId + "' historic : \r\n");
        for (String s : messagesHistoric) {
            historic.append(s).append("\n");
        }
        return historic.toString();
    }

    public void rename(String name, Client author) {
        roomName = name;
        broadcast(author.name + " renamed this room to '" + name + "'");
    }

}
