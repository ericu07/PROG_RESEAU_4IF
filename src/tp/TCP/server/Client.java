package tp.TCP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

    // IO

    private final BufferedReader socIn;
    private final PrintStream socOut;

    // Attributs

    protected String name;
    protected Room room;


    // Constructeur

    public Client(Socket clientSocket) throws IOException {
        socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        socOut = new PrintStream(clientSocket.getOutputStream());

        send("Enter your name");
        name = receive();
        send("Hello " + name + " !");
    }

    // Méthodes

    public void send(String message) {
        socOut.println(message);
    }

    public String receive() throws IOException {
        return socIn.readLine();
    }

    public void changeRoom(Room newRoom) {
        Room oldRoom = this.room;

        this.room = null;
        if (oldRoom != null) {
            oldRoom.members.remove(this);
            oldRoom.broadcast(name + " leaved the room");
        }

        this.room = newRoom;
        if (newRoom != null) {
            send(newRoom.historic());
            room.members.add(this);
            room.broadcast(name + " entered the room");
        }
    }

    public void rename(String name) {
        String oldName = this.name;
        this.name = name;
        if (room != null) { // nom par défaut, pas encore de salle
            room.broadcast(oldName + " renamed to " + name);
        }
    }

}
