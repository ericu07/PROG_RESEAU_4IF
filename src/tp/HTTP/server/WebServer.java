///A Simple Web Server (WebServer.java)

package tp.HTTP.server;

import java.util.List;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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

    static final List<String> SUPPORTED_DYNAMIC_CONTENT = Arrays.asList(new String[]{"class"});

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
        for (;;) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                new Thread(() -> handleClient(remote)).start();
            } catch (Exception e) {
                System.out.println("Error: " + e);
                e.printStackTrace();
            }
        }
    }

    protected void handleClient(Socket remote) {
        try {
            // remote is now the connected socket
            System.out.println("Connection, sending data.");
            BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
            PrintStream out = new PrintStream(remote.getOutputStream());

            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            LinkedList<String> header = new LinkedList<>();
            String str;
            do {
                System.out.print("head : ");
                str = in.readLine();
                header.addLast(str);
                System.out.println(str);
            } while (str != null && !str.equals(""));

            String requestType = header.get(0).split(" ")[0];
            String resourceName = header.get(0).split(" ")[1];

            switch (requestType) { // request type
                case "HEAD":
                case "GET":
                case "POST": {
                    // head, get, post have lot in common

                    // File to read
                    if (resourceName.equals("/")) { resourceName = "index.html"; }
                    boolean error404 = false;
                    File fileToRead = new File("resources/" + resourceName);
                    if (!fileToRead.exists()) {
                        error404 = true;
                        System.out.println("404: " + fileToRead.getAbsolutePath());

                        resourceName = "404.html";
                        fileToRead = new File("resources/" + resourceName);
                    }

                    // Send the response

                    // Send the headers
                    out.println(error404 ? "HTTP/1.0 404 NOT FOUND" : "HTTP/1.0 200 OK"); // code d'erreur
                    out.println("Content-Type: " + mimeType(fileToRead));
                    out.println("Server: Bot");
                    // this blank line signals the end of the headers
                    out.println();

                    // read POST data
                    if (requestType.equals("POST")) {
                        int bodyLenght = 0; // request's body length
                        for (String headerLine : header) {
                            if (headerLine.split(" ")[0].equals("Content-Length:")) {
                                bodyLenght = Integer.parseInt(headerLine.split(" ")[1]);
                            }
                        }
                        int bodyReadedLenght = 0;

                        while (bodyReadedLenght < bodyLenght) {
                            System.out.print("body : ");
                            str = in.readLine();
                            header.addLast(str);
                            System.out.println(str);
                            bodyReadedLenght += 1; // sauts de ligne
                            bodyReadedLenght += str.length();
                        }
                    }

                    // Send the HTML page
                    if (!requestType.equals("HEAD")) { // not send for head

                        if (isDynamicContent(resourceName)) { // java dynamic resource
                            Queue<String> contentOutput = runDynamicContent(fileToRead);
                            while (!contentOutput.isEmpty()) {
                                out.println(contentOutput.remove());
                            }

                            out.println();
                            out.flush();

                        } else { // texte
                            byte[] bytes = Files.readAllBytes(fileToRead.toPath());
                            out.write(bytes, 0, bytes.length);

                            out.println();
                            out.flush();
                        }
                    }

                    break;
                }
                case "DELETE": {
                    String resource = resourceName.split("\\?")[0];
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
                    String resource = resourceName.split("\\?")[0];
                    File f = new File("resources/" + resource);
                    StringBuilder fContent = new StringBuilder();
                    try {
                        if (f.createNewFile()) {
                            int bodyLenght = 0;
                            for (String headerLine : header) {
                                if (headerLine.split(" ")[0].equals("Content-Length:")) {
                                    bodyLenght = Integer.parseInt(headerLine.split(" ")[1]);
                                }
                            }
                            int bodyReadedLenght = 0;

                            while (bodyReadedLenght < bodyLenght) {
                                System.out.print("body : ");
                                str = in.readLine();
                                header.addLast(str);
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
            e.printStackTrace();
        }
    }

    protected Queue<String> runDynamicContent(File fileToRead) throws IOException {
        Queue<String> out = new LinkedList<>();

        Process p = Runtime.getRuntime().exec("java -cp " + fileToRead.getParentFile().getAbsolutePath() + " " + fileToRead.getName().split("\\.")[0]); // run the java file
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            out.add(line); // print dynamic resource output
        }
        input.close();

        return out;
    }

    protected String mimeType(File file) throws IOException {
        return isDynamicContent (file.getName()) ? "text/html" : Files.probeContentType(file.toPath());
    }

    protected boolean isDynamicContent(String resourceName) {
        String[] parts = resourceName.split("\\.");
        return parts.length > 1 && SUPPORTED_DYNAMIC_CONTENT.contains(parts[1]);
    }
}
