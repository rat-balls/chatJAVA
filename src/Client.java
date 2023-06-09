import java.io.*;
import java.net.Socket;


public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    @Override
    public void run() {
        try{
            client = new Socket("6.tcp.eu.ngrok.io", 19437);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();
            String inputMessage;
            while((inputMessage = in.readLine()) != null){
                System.out.println(inputMessage);
            }
        }catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch(IOException e){

        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inputReader.readLine();
                    if (message.equals("/quit")) {
                        out.println(message);
                        inputReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
