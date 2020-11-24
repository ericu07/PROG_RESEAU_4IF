package tp.TCP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Server side, TCP
 */
public class ChatServer {

    /**
     * Available rooms
     */
    static Dictionary<Integer, Room> rooms = new Hashtable<>();

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }

        rooms.put(0, new Room(0, "default room")); // default room;

        try {
            ServerSocket listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port

            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("connexion from:" + clientSocket.getInetAddress());
                new Thread( () -> handleClient(clientSocket) ).start();
            }
        } catch (Exception e) {
            System.err.println("handled exception");
            e.printStackTrace();
        }
    }

    /**
     * Handle a client, run in a dedicated thread
     * @param clientSocket socket linked to the client
     */
    private static void handleClient(Socket clientSocket) {
        Client client = null;

        try { // ouverture du client
            client = new Client(clientSocket);
        } catch (IOException e) {
            System.err.println("handled exception");
            e.printStackTrace();
        }

        if (client != null) { // le client est ouvert
            try {
                client.changeRoom(rooms.get(0)); // on va dans la salle par defaut
                while (true) {

                    String line = client.receive();
                    if (isCommand(line)) { // une commande
                        switch (commandName(line)) {
                            case "name": {
                                client.rename(commandAttribute(line, 1));
                                break;
                            }
                            case "roominfo": {
                                client.send(client.room.toString());
                                break;
                            }
                            case "rooms": {
                                client.send(roomsList(rooms.elements()));
                                break;
                            }
                            case "createroom" : {
                                createRoom(client, line);
                                break;
                            }
                            case "renameroom" : {
                                client.room.rename(commandAttribute(line, 1), client);
                                break;
                            }
                            case "join" : {
                                if ( (commandAttribute(line, 1) != null) &&
                                     (rooms.get(new Integer(commandAttribute(line, 1))) != null) ) {
                                    client.changeRoom(rooms.get(new Integer(commandAttribute(line, 1))));
                                } else {
                                    client.send("invalid room");
                                }
                                break;
                            }
                            case "exit": {
                                break;
                            }
                            case "historic": {
                                client.send(client.room.historic());
                                break;
                            }
                            case "savehistoric": {
                                boolean save = client.room.saveHistoric();
                                if(save) {
                                    client.send("Historic saved");
                                }else{
                                    client.send("Fail to save historic");
                                }
                                break;
                            }
                            case "loadhistoric": {
                                boolean restoration = client.room.loadHistoric();
                                if(restoration) {
                                    client.send("Load Successfull");
                                    client.room.broadcast(client.name + " has load the historic of room " + client.room.roomId);
                                }else{
                                    client.send("Load Failure");
                                }
                                break;
                            }
                            case "help": {
                                client.send("!name <a> -> rename the user\r\n" +
                                        "!roominfo -> get room's info\r\n"+
                                        "!rooms-> list of the rooms\r\n"+
                                        "!createroom <1> -> create a room with a number\r\n"+
                                        "!renameroom <1> -> rename a room with a number\r\n"+
                                        "!join <1> -> join a room with a room number\r\n"+
                                        "!exit -> exit the chat\r\n"+
                                        "!historic -> show room's historic\r\n"+
                                        "!savehistoric -> save room's historic\r\n"+
                                        "!loadhistoric -> load a room's historic\r\n"+
                                        "!help -> get list of commands\r\n"
                                );
                            }

                            default: {
                                client.send("unknown command : '" + line + "'"+"\r\nTry !help for more information");
                                break;
                            }
                        }
                    } else if (isMessage(line)) { // un message
                        try{
                            client.room.broadcast("> " + client.name + " : " + line);
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (IOException e) {
                System.err.println("handled exception");
                e.printStackTrace();
            } finally {
                client.changeRoom(null);
            }
        }
    }

    /**
     * Check if a string begin with '!'
     * @param s string to check
     * @return s begin with '!'
     */
    private static boolean isCommand(String s) {
        return s != null && s.length() > 0 && s.charAt(0) == '!';
    }

    /**
     * Extract command name from a command (!<name> ...)
     * @param command command
     * @return command name
     */
    private static String commandName(String command) {
        try {
            return command.split(" ", 2)[0].substring(1).toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extract an attribute from a command (!<name> [...] [...] [...])
     * @param command command
     * @param attribute attribute number, 1 is the first attribute
     * @return command attribute
     */
    private static String commandAttribute(String command, int attribute) {
        try {
            return command.split(" ", 2)[attribute];
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if a string is a message
     * @param s string to check
     * @return s is a message
     */
    private static boolean isMessage(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * Create a list of the available rooms
     * @param rooms available rooms
     * @return a string describing available rooms
     */
    private static String roomsList(Enumeration<Room> rooms) {
        StringBuilder sb = new StringBuilder("Rooms list :\r\n");
        while (rooms.hasMoreElements()) {
            sb.append("\t").append(rooms.nextElement().toString()).append("\r\n");
        }
        return(sb.toString());
    }

    /**
     * create a room
     * @param client who created the room
     * @param command command used to create the room
     */
    private static void createRoom(Client client, String command) {
        Integer roomNumber;

        try { // lecture du numéro de salle
            String roomNumberAsString = commandAttribute(command, 1);
            roomNumber = Integer.parseInt(roomNumberAsString);
        } catch (Exception e) {
            client.send("invalid parameter(s)");
            return;
        }

        if (rooms.get(roomNumber) == null) {
            Room r = new Room(roomNumber);
            rooms.put(roomNumber, r);
            client.send("The room '" + roomNumber + "' was created");
        } else {
            client.send("The room '" + roomNumber + "' already exist");
        }
    }

}
