package tp.TCP.server;

import java.io.*;
import java.util.LinkedList;

/**
 * Data used to represent a room
 */
public class Room {

    // Attributs

    protected Integer roomId;
    protected String roomName = null;

    protected LinkedList<String> messagesHistoric = new LinkedList<>();

    protected LinkedList<Client> members = new LinkedList<>();

    // Constructeurs

    /**
     * create a room with an id
     * @param id the room id
     */
    public Room(Integer id) {
        roomId = id;
        broadcast("Room " + id + " created");
    }

    /**
     * create a room with an id and a name
     * @param id the room id
     * @param name the room name
     */
    public Room(Integer id, String name) {
        roomId = id;
        roomName = name;
        broadcast("Room " + id + " : '" + name + "' created");
    }

    // Méthodes

    /**
     * String representation of the room (Room <id> (<nb> users))
     * @return string representation
     */
    public String toString() {
        if (roomName == null) {
            return "Room " + roomId + " (" + members.size() + " users)";
        } else {
            return "Room " + roomId + " : " + roomName  + " (" + members.size() + " users)";
        }
    }

    /**
     * emit a message to all room members
     * @param message
     */
    public void broadcast(String message) {
        messagesHistoric.addLast(message);
        for (Client c : members) {
            c.send(message);
        }
    }

    /**
     * String representation of the room historic
     * @return string representation
     */
    public String historic() {
        StringBuilder historic = new StringBuilder("Room '" + roomId + "' historic : \r\n");
        for (String s : messagesHistoric) {
            historic.append(s).append("\n");
        }
        return historic.toString();
    }

    /**
     * Rename a room
     * @param name new name for the room
     * @param author who renamed it
     */
    public void rename(String name, Client author) {
        roomName = name;
        broadcast(author.name + " renamed this room to '" + name + "'");
    }

    /**
     * save on the server the room historic
     * @return success
     */
    public boolean saveHistoric() {
        FileWriter fw = null;
        String folderPath;
        String filePath;
        try {
            folderPath = System.getProperty("user.home") + "/Documents/Temp/tp_reseau";
            File folder = new File(folderPath);

            if(!folder.exists()) {
                folder.mkdirs();
            }

            filePath = folderPath + "/" + this.roomId + ".txt";
            File f =  new File(filePath);

            if(!f.exists()) {
                f.createNewFile();
            }

            fw = new FileWriter(f, false);
            PrintWriter pw = new PrintWriter(fw);
            for(String msg : messagesHistoric) {
                pw.println(msg);
            }

            pw.close();
            fw.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * replace current historic with the one stored on the server, if any exist
     * @return success
     */
    public boolean loadHistoric () {
        String filePath;

        try {
            filePath = System.getProperty("user.home")
                    + "/Documents/Temp/tp_reseau" + "/" + this.roomId + ".txt";
            File f =  new File(filePath);

            if(!f.exists()) {
                return false;
            }else{
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String line = "";
                this.messagesHistoric.clear();
                while((line=br.readLine())!=null){
                    this.messagesHistoric.addLast(line);
                }
                br.close();
                fr.close();
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

}
