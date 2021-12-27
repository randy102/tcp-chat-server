import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Client1 {
    private static String host = "localhost";
    private static int port = 1234;
    private static Socket socket;

    private static BufferedWriter out;
    private static BufferedReader in;

    static class SendMessage implements Runnable {
        private BufferedWriter out;
        private Socket socket;
        public SendMessage(Socket s, BufferedWriter o) {
            this.socket = s;
            this.out = o;
        }
        public void run() {
            try {
                while(true) {
                    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                    String data = stdIn.readLine();
                    out.write(data+'\n');
                    out.flush();
                    if(data.equals("bye"))
                        break;
                }
                System.out.println("Client closed connection");
                out.close();
                socket.close();
            } catch (IOException e) {}
            finally {
                System.out.println("Exit");
            }
        }
    }

    static class ReceiveMessage implements Runnable {
        private BufferedReader in;
        private Socket socket;
        public ReceiveMessage(Socket s, BufferedReader i) {
            this.socket = s;
            this.in = i;
        }
        public void run() {
            try {
                while(true) {
                    String data = in.readLine();
                    System.out.println("Receive: " + data);
                    if(data == null){
                        break;
                    }
                }
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        socket = new Socket(host, port);
        System.out.println("Client connected");
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        SendMessage send = new SendMessage(socket, out);
        ReceiveMessage recv = new ReceiveMessage(socket, in);
        executor.execute(send);
        executor.execute(recv);
    }
}