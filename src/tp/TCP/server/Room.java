package tp.TCP.server;

import java.io.*;
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
