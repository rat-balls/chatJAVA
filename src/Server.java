import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        done = false;
        connections = new ArrayList<>();
    }
    @Override
    public void run(){
        try {
            server = new ServerSocket(9070);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }

    }
    public void broadcast(String message){
        for (ConnectionHandler ch : connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler ch : connections){
                ch.shutdown();
            }
        }catch (IOException e){

        }
    }


    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private  String nickname;
        public ConnectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try{
                File logFile = new File("src/logs.txt");
                if (logFile.createNewFile()) {
                    System.out.println("File created: " + logFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                //condition name
                System.out.println(nickname + " connected");
                broadcast("Server: " + nickname + " joined");
                logMessage("Server: " + nickname + " joined\n");
                String message;
                while((message = in.readLine()) != null) {
                    if (message.startsWith("/changename")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast("Server: " + nickname + " name change into : " + messageSplit[1]);
                            logMessage("Server: " + nickname + " name change into : " + messageSplit[1] + "\n");
                            nickname = messageSplit[1];
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast("Server: " + nickname + " left");
                        logMessage("Server: " + nickname + " left\n");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                        logMessage(nickname + ": " + message + "\n");
                    }
                }
            } catch (IOException e){
                System.out.println("An error occurred.");
                e.printStackTrace();
                shutdown();
            }
        }
        public void sendMessage(String message){
                out.println(message);
        }

        public void logMessage(String message) throws IOException {
            FileWriter logWrite = new FileWriter("src/logs.txt", true);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String date = dateFormat.format(new Date());
            logWrite.write(date + "      "  + message);
            logWrite.close();
        }
        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch (IOException e){

            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
