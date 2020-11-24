package tp.TCP.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client side, TCP
 */
public class ChatClient {

    static Thread write = null;
    static Thread read = null;

    static Socket serverSocket = null;
    static PrintStream socOut = null;
    static BufferedReader stdIn = null;
    static BufferedReader socIn = null;

    static JFrame f;
    static JTextArea display;
    static JScrollPane scroll;
    static JTextField zoneText;
    static JButton sendText;

    /**
     * Handle user inputs and forward them to the server, run in a dedicated thread
     */
    private static void readThread() {
        while (true) {
            try {
                String line = stdIn.readLine();
                socOut.println(line);
                display.append(line+"\r\n");
                if (line.equals("!exit")) {
                    write.interrupt();
                    f.dispose();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        display.append("Disconected");
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

    /**
     * Handle messages reception from the server and display them, run in a dedicated thread
     */
    private static void writeThread() {
        while (true) {
            try {
                String transi = socIn.readLine();
                System.out.println(transi);
                if(display!=null) {
                    display.append(transi+"\r\n");
                }
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

        read = new Thread(ChatClient::readThread);
        read.start();
        write = new Thread(ChatClient::writeThread);
        write.start();

        f = new JFrame();
        display = new JTextArea ();
        display.append("Enter your name\r\n");
        display.setEditable (false);

        scroll = new JScrollPane (display);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(0,0,800,600);

        zoneText = new JTextField();
        zoneText.setBounds(0,600, 800,100);
        f.add(zoneText);
        f.add(scroll);

        sendText = new JButton("Send Message");
        sendText.setBounds(300,705,200,100);
        sendText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String transi = zoneText.getText();
                socOut.println(transi);
                zoneText.setText("");
                if(transi.equals("!exit")){
                    f.dispose();
                    write.interrupt();
                    read.interrupt();
                    System.exit(0);
                }
            }
        });

        f.add(sendText);
        f.setSize(825,900);
        f.setLayout(null);
        f.setVisible(true);

    }

}
