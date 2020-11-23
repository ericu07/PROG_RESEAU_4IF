package tp.TCP.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {

    static Thread write = null;

    static Socket serverSocket = null;
    static PrintStream socOut = null;
    static BufferedReader stdIn = null;
    static BufferedReader socIn = null;

    private static void readThread() {
        while (true) {
            try {
                String line = stdIn.readLine();
                socOut.println(line);
                if (line.equals("!exit")) {
                    write.interrupt();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("Disconected");
        try {
            socOut.close();
            socIn.close();
            stdIn.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeThread() {
        while (true) {
            try {
                System.out.println(socIn.readLine());
            } catch (IOException e) {
                System.out.println("handled exception");
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            // creation socket ==> connexion
            serverSocket = new Socket(args[0], Integer.parseInt(args[1]));
            socIn = new BufferedReader(
                    new InputStreamReader(serverSocket.getInputStream()));
            socOut= new PrintStream(serverSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to " + args[0]);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:"+ args[0]);
            System.exit(1);
        }

        new Thread(ChatClient::readThread).start();
        write = new Thread(ChatClient::writeThread);
        write.start();
    }

}
