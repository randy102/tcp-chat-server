package libs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class IO {
    BufferedReader in;
    BufferedWriter out;
    final Socket socket;
    final String id;

    public interface Handler {
        void onRequest(Message message) throws IOException;
    }

    public IO(Socket s, String id) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        this.socket = s;
        this.id = id;
    }

    public void send(Message message) throws IOException {
        System.out.println("Sending " + message.toString() + " to " + this.id);
        this.out.write(message.toString());
        this.out.newLine();
        this.out.flush();
    }

    public void handle(Handler handler) throws IOException {
        System.out.println("Client " + id + " joined!");
        try {
            String input = "";
            do {
                input = in.readLine();
                System.out.println("Server received: " + input + " from # Client " + id);
                try {
                    JSONObject json = new JSONObject(input);
                    handler.onRequest(new Message(json));
                } catch (IOException e){
                    this.send(new Message(Command.ERROR, e.getMessage()));
                }
            } while (!input.equals("bye"));
            System.out.println("Closed socket for client " + id + " " + socket.toString());

        } catch (IOException | JSONException e) {
            System.out.println(e);
        } finally {
            in.close();
            out.close();
            socket.close();
        }
    }
}
