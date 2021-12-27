import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static int port = 1234;
    public static int numThread = 2;
    private static ServerSocket server = null;
    public static HashMap<String,Client> clients = new HashMap<>();
    public static Set<String> waitingList = new HashSet<>();

    public static void main(String[] args) throws IOException {
        int i = 0;
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            server = new ServerSocket(port);
            System.out.println("Server binding at port " + port);
            System.out.println("Waiting for client...");
            while(true) {
                i++;
                Socket socket = server.accept();
                Client client = new Client(socket, Integer.toString(i));
                executor.execute(client);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if(server!=null)
                server.close();
        }
    }
}