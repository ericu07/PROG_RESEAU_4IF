package tp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientsTCP {

    static Thread write = null;
    static Socket echoSocket = null;
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
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeThread() {
        while (true) {
            try {
                System.out.println(socIn.readLine());
            } catch (IOException e) {
                System.out.println("catch exception");
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
            echoSocket = new Socket(args[0],new Integer(args[1]).intValue());
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut= new PrintStream(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:"+ args[0]);
            System.exit(1);
        }

        new Thread(ChatClientsTCP::readThread).start();
        write = new Thread(ChatClientsTCP::writeThread);
        write.start();
    }

}
