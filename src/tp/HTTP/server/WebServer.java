///A Simple Web Server (WebServer.java)

package tp.HTTP.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedList;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * <p>
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

    /**
     * Start the application.
     *
     * @param args Command line parameters are not used.
     */
    public static void main(String[] args) {
        WebServer ws = new WebServer();
        ws.start();
    }

    /**
     * WebServer constructor.
     */
    protected void start() {
        ServerSocket s;

        System.out.println("Webserver starting up on port 3000");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (; ; ) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        remote.getInputStream()));
                PrintStream out = new PrintStream(remote.getOutputStream());

                // read the data sent. We basically ignore it,
                // stop reading once a blank line is hit. This
                // blank line signals the end of the client HTTP
                // headers.
                LinkedList<String> strs = new LinkedList<>();
                String str;
                do {
                    System.out.print("head : ");
                    str = in.readLine();
                    strs.addLast(str);
                    System.out.println(str);
                } while (str != null && !str.equals(""));

                String[] split = strs.get(0).split(" ");
                System.out.println(split[0]);
                switch (split[0]) {
                    case "HEAD":
                    case "GET":
                    case "POST": {
                        // File
                        boolean error404 = false;
                        String resource = split[1].split("\\?")[0];
                        File f = new File("resources/" + resource);
                        if (!f.exists()) {
                            System.out.println("404: " + f.getAbsolutePath());
                            error404 = true;
                            f = new File("resources/404.html");
                        }

                        // Send the response

                        // Send the headers
                        out.println(error404 ? "HTTP/1.0 404 NOT FOUND" : "HTTP/1.0 200 OK");
                        out.println("Content-Type: text/html");
                        out.println("Server: Bot");
                        // this blank line signals the end of the headers
                        out.println();

                        // Send the HTML page
                        if (split[0].equals("POST")) {
                            int bodyLenght = 0;
                            for (String headerLine : strs) {
                                if (headerLine.split(" ")[0].equals("Content-Length:")) {
                                    bodyLenght = Integer.parseInt(headerLine.split(" ")[1]);
                                }
                            }
                            int bodyReadedLenght = 0;

                            while (bodyReadedLenght < bodyLenght) {
                                System.out.print("body : ");
                                str = in.readLine();
                                strs.addLast(str);
                                System.out.println(str);
                                bodyReadedLenght += 1; // sauts de ligne
                                bodyReadedLenght += str.length();
                            }
                        }

                        // Send the HTML page
                        if (!split[0].equals("HEAD")) {
                            FileReader fr = new FileReader(f);
                            BufferedReader br = new BufferedReader(fr);

                            String line;
                            while((line=br.readLine())!=null) {
                                out.println(line);
                            }
                            out.println("");
                            out.println();
                            out.flush();

                            br.close();
                            fr.close();
                        }

                        break;
                    }
                    case "DELETE": {
                        String resource = split[1].split("\\?")[0];
                        File f = new File("resources/" + resource);
                        if (f.exists() && f.delete()) {
                            System.out.println("File " + f.getAbsolutePath() + " deleted");
                            // Send the headers
                            out.println("HTTP/1.0 202 ACCEPTED");
                            out.println("Content-Type: text/html");
                            out.println("Server: Bot");
                            // this blank line signals the end of the headers
                            out.println();
                        } else {
                            System.out.println("Failed to delete " + f.getAbsolutePath());
                            // Send the headers
                            out.println("HTTP/1.0 204 NO CONTENT");
                            out.println("Content-Type: text/html");
                            out.println("Server: Bot");
                            // this blank line signals the end of the headers
                            out.println();
                        }
                        break;
                    }
                    case "PUT": {
                        String resource = split[1].split("\\?")[0];
                        File f = new File("resources/" + resource);
                        StringBuilder fContent = new StringBuilder();
                        try {
                            if (f.createNewFile()) {
                                int bodyLenght = 0;
                                for (String headerLine : strs) {
                                    if (headerLine.split(" ")[0].equals("Content-Length:")) {
                                        bodyLenght = Integer.parseInt(headerLine.split(" ")[1]);
                                    }
                                }
                                int bodyReadedLenght = 0;

                                while (bodyReadedLenght < bodyLenght) {
                                    System.out.print("body : ");
                                    str = in.readLine();
                                    strs.addLast(str);
                                    fContent.append(str).append("\r\n");
                                    System.out.println(str);
                                    bodyReadedLenght += 1; // sauts de ligne
                                    bodyReadedLenght += str.length();
                                }
                                BufferedWriter fWriter = new BufferedWriter(new FileWriter(f));
                                fWriter.append(fContent.toString());
                                fWriter.close();

                                System.out.println("Created file " + f.getAbsolutePath());
                                // Send the headers
                                out.println("HTTP/1.0 201 CREATED");
                                out.println("Content-Location: /" + resource);
                                out.println("Server: Bot");
                                // this blank line signals the end of the headers
                                out.println();
                            } else {
                                throw new IOException();
                            }
                        } catch (IOException e) {
                            System.out.println("Failed to create file " + f.getAbsolutePath());
                            // Send the headers
                            out.println("HTTP/1.0 204 No Content");
                            out.println("Content-Location: /" + resource);
                            out.println("Server: Bot");
                            // this blank line signals the end of the headers
                            out.println();

                        }
                        break;
                    }
                    default: {
                        // Send the response

                        // Send the headers
                        out.println("HTTP/1.0 200 OK");
                        out.println("Content-Type: text/html");
                        out.println("Server: Bot");
                        // this blank line signals the end of the headers
                        out.println();

                        // Send the HTML page
                        out.println("<H1>Welcome to the Ultra Mini-WebServer</H1>");

                        out.flush();
                        break;
                    }
                }

                out.println();
                remote.close();
                System.out.println("request done");

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }
}
