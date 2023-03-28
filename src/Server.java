import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;

    @Override
    public void run() {
        try{
            ServerSocket server = new ServerSocket(4444);
            Socket client = server.accept();
            ConnectionHandler handeler = new ConnectionHandler(client);
            connections.add(handeler);
        }catch (IOException e){
            //TODO: handle
        }
    }
    public void broadcast(String message) {
        for(ConnectionHandler ch:connections){
            if(ch !=null){
                ch.sendMessage(message);
            }
        }
        
    }
    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client){
            this.client = client;
        }
        
        @Override
        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname +" is connected");

            } catch (IOException e) {
                // TODO: handle
            }

        }
        public void sendMessage(String message){
             out.println(message);
        }
    }
    
}
