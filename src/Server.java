import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            // Create a ServerSocket that listens on port 4444
            ServerSocket server = new ServerSocket(4444);
            pool = Executors.newCachedThreadPool();
            // Continuously accept incoming connections and create a new ConnectionHandler for each one
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handeler = new ConnectionHandler(client);
                connections.add(handeler);
                pool.execute(handeler);
            }

        } catch (IOException e) {
            shutdown();
        }
    }

    // Broadcast a message to all connected clients
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    // Close the ServerSocket and all client connections
    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // Ignore any IOException that occurs
        }
    }

    // A class to handle a single client connection
    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        // Initialize the ConnectionHandler with the client Socket
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                // Initialize input and output streams for the client Socket
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Ask the client to enter a nickname
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " is connected");

                // Broadcast a message to all clients that a new client has joined
                broadcast(nickname + " joined the chat!");

                String message;
                // Continuously listen for messages from the client

                while ((message = in.readLine()) != null) {

                    if (message.startsWith("/nick ")) {
                        // If the client sends a "/nick" command, update their nickname and broadcast a message to all clients
                        String[] messageSplit = message.split(" ", 2);

                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Succesfully changed the nickname to " + nickname);
                        } else {
                            out.println("No nickname provided!");
                        }

                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname+" left the chat");
                        shutdown();

                    } else {
                        // Broadcast the client's message to all clients
                        broadcast(nickname + ": " + message);
                    }
                }

            } catch (IOException e) {
                shutdown();
            }

        }

        // Send a message to the client
        public void sendMessage(String message) {
            out.println(message);
        }

        // Close the client Socket and associated streams
        public void shutdown() {
            try {
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }

            } catch (IOException e) {
                // TODO: handle
            }

        }
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
   }
}
