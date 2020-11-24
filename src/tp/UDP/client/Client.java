package tp.UDP.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Client side (no server associated, use multicast)
 */
public class Client {

    static Thread write = null;

    static BufferedReader stdIn = null;
    static MulticastSocket mSocket = null;
    static int groupPort = 0;
    static InetAddress groupAddr = null;

    public static void main(String[] args) {


        if (args.length != 2) {
            System.out.println("Usage: java Client <Client Mulitcast IP> <Client Mulitcast port>");
            System.exit(1);
        }

        try {

            // Group IP address
            groupAddr = InetAddress.getByName(args[0]);
            groupPort = Integer.parseInt(args[1]);

            stdIn = new BufferedReader(new InputStreamReader(System.in));

            // Create a multicast socket
            mSocket = new MulticastSocket(groupPort);
            //Join the group
            mSocket.joinGroup(groupAddr);


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:"+ args[0]);
            System.exit(1);
        }

        new Thread(Client::readThread).start();
        write = new Thread(Client::writeThread);
        write.start();


    }

    /**
     * Handle user inputs and forward them to the server, run in a dedicated thread
     */
    private static void readThread() {
        while (true) {
            try {
                String line = stdIn.readLine();
                if (line.equals("!exit")) {
                    write.interrupt();
                    break;
                }else{
                    byte[] buf = line.getBytes();
                    DatagramPacket bouteilleALaMer = new  DatagramPacket(buf, buf.length, groupAddr, groupPort);
                    mSocket.send(bouteilleALaMer);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("Disconected");
        try {
            stdIn.close();
            mSocket.leaveGroup(groupAddr);
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle messages reception from the server and display them, run in a dedicated thread
     */
    private static void writeThread() {
        while (true) {
            try {
                byte[] buf = new byte[1000];
                DatagramPacket recv = new  DatagramPacket(buf, buf.length);
                mSocket.receive(recv);

                byte[] newBuf = Arrays.copyOfRange(buf, 0, recv.getLength());

                String msg = new String(newBuf, StandardCharsets.UTF_8);
                System.out.println(msg);

            } catch (IOException e) {
                System.out.println("handled exception");
                e.printStackTrace();
                break;
            }
        }
    }

}
