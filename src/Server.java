import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
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
                String systemColor = "\033[0;33m";
                String defaultColor = "\033[0m";
                String color = "\033[0;32m";
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println(systemColor + "Please enter a nickname: " + defaultColor);
                nickname = in.readLine();
                //condition name
                System.out.println(nickname + " connected");
                broadcast(systemColor + "Server: " + nickname + " joined" + defaultColor);
                logMessage("Server: " + nickname + " joined\n");
                String message;
                while((message = in.readLine()) != null) {
                    String[] messageSplit = message.split(" ", 2);
                    if (message.startsWith("/nick ")) {
                        if (messageSplit.length == 2) {
                            broadcast(systemColor + "Server: " + color + nickname + systemColor + " name change into : " + messageSplit[1] + defaultColor);
                            logMessage("Server: " + nickname + " name change into : " + messageSplit[1] + "\n");
                            nickname = messageSplit[1];
                        }
                    }else if(message.startsWith("/color ")){
                        if(Objects.equals(messageSplit[1], "red")){
                            color = "\033[0;31m";
                        }else if(Objects.equals(messageSplit[1], "green")){
                            color = "\033[0;32m";
                        }else if(Objects.equals(messageSplit[1], "blue")){
                            color = "\033[0;34m";
                        }else if(Objects.equals(messageSplit[1], "purple")){
                            color = "\033[0;35m";
                        }else if(Objects.equals(messageSplit[1], "cyan")){
                            color = "\033[0;36m";
                        }else if(Objects.equals(messageSplit[1], "lred")){
                            color = "\033[1;31m";
                        }else if(Objects.equals(messageSplit[1], "lpurple")){
                            color = "\033[1;35m";
                        }
                        broadcast(systemColor + "Server: " + color + nickname + " has changed colors." + defaultColor);

                    } else if (message.startsWith("/quit")) {
                        broadcast(systemColor + "Server: " + nickname + " left" + defaultColor);
                        logMessage("Server: " + nickname + " left\n");
                        shutdown();
                    } else {
                        broadcast(color + nickname + ": " + message + defaultColor);
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
