package tp.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServerTCP {

    static int userCounter = 0;

    static Dictionary<Integer, Room> rooms = new Hashtable<>();

    private static void handleClient(Socket clientSocket) {
        Client client = new Client();

        try {
            client.socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            client.socOut = new PrintStream(clientSocket.getOutputStream());

            client.name = "" + userCounter++;

            joinRoom(client, rooms.get(0));

            while (true) {
                String line = client.socIn.readLine();
                if (line.length() > 0) {
                    if (line.charAt(0) == '!') { // une commande
                        switch (line.split(" ", 2)[0]) {
                            case "!name": {
                                rename(client, line.split(" ", 2)[1]);
                                break;
                            }
                            case "!rooms": {
                                rooms(client, rooms.elements());
                                break;
                            }
                            case "!createroom" : {
                                if (rooms.get(Integer.parseInt(line.split(" ", 2)[1])) == null) {
                                    createRoom(client, Integer.parseInt(line.split(" ", 2)[1]) );
                                } else {
                                    emitMessage(client, "sorry, bro");
                                }
                                break;
                            }
                            case "!join" : {
                                Room roomTo = rooms.get(Integer.parseInt(line.split(" ", 2)[1]));
                                if (roomTo != null) {
                                    leaveRoom(client);
                                    joinRoom(client, roomTo);
                                } else {
                                    emitMessage(client, "sorry, bro");
                                }
                                break;
                            }
                            case "!exit": {
                                leaveRoom(client);
                            }
                            case "!historic": {
                                historic(client);
                            }
                            default: {
                                break;
                            }
                        }
                    } else { // pas une commande
                        broadcast(client.room, client.name + " : " + line);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
            leaveRoom(client);
        }
    }

    private static void historic(Client client) {
        StringBuilder historic = new StringBuilder();
        for (String s : client.room.messagesHistoric) {
            historic.append(s).append("\n");
        }
        emitMessage(client, historic.toString());
    }

    private static void rename(Client client, String name) {
        String oldName = client.name;
        client.name = name;
        broadcast(client.room, oldName + " renamed to " + name);
    }

    private static void rooms(Client client, Enumeration<Room> rooms) {
        StringBuilder sb = new StringBuilder("Rooms list :\r\n");
        while (rooms.hasMoreElements()) {
            sb.append("\t").append(rooms.nextElement().roomId).append("\r\n");
        }
        emitMessage(client, sb.toString());
    }

    private static void createRoom(Client client, Integer roomId) {
        rooms.put(roomId, new Room(roomId));
        emitMessage(client, "The room '" + roomId + "' was created");
    }

    private static void joinRoom(Client client, Room room) {
        client.room = room;
        room.members.add(client);
        historic(client);
        broadcast(room, client.name + " entered the room");
    }

    private static void leaveRoom(Client client) {
        Room room = client.room;
        client.room = null;
        room.members.remove(client);
        broadcast(room, client.name + " leaved the room");
    }

    private static void broadcast(Room room, String message) {
        room.messagesHistoric.addLast(message);
        for (Client c : room.members) {
            emitMessage(c, message);
        }
    }

    private static void emitMessage(Client client, String message) {
        client.socOut.println(message);
    }

    public static void main(String args[]) {
        ServerSocket listenSocket;

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            rooms.put(0, new Room(0));
            rooms.put(1, new Room(1));

            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("connexion from:" + clientSocket.getInetAddress());
                new Thread( () -> handleClient(clientSocket) ).start();
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

}
