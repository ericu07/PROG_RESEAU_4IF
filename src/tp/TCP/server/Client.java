package tp.TCP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Data used to represent a client
 */
public class Client {

    // IO

    private final BufferedReader socIn;
    private final PrintStream socOut;

    // Attributs

    protected String name;
    protected Room room;


    // Constructeur

    /**
     * Create the client abstraction
     * @param clientSocket socket associated to the client
     * @throws IOException exception
     */
    public Client(Socket clientSocket) throws IOException {
        socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        socOut = new PrintStream(clientSocket.getOutputStream());

        send("Enter your name");
        name = receive();
        send("Hello " + name + " !");
    }

    // Méthodes

    /**
     * send a message to the client
     * @param message message to send
     */
    public void send(String message) {
        socOut.println(message);
    }

    /**
     * receive a message from the client
     * @return message received
     * @throws IOException exception
     */
    public String receive() throws IOException {
        return socIn.readLine();
    }

    /**
     * change the room of the client
     * @param newRoom room to enter
     */
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

    /**
     * rename the client
     * @param name new name for the client
     */
    public void rename(String name) {
        String oldName = this.name;
        this.name = name;
        if (room != null) { // nom par défaut, pas encore de salle
            room.broadcast(oldName + " renamed to " + name);
        }
    }

}
