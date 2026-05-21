package ChatApp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   Telegram Clone  —  Server       ║");
        System.out.println("╚══════════════════════════════════╝");


        DatabaseManager db = new DatabaseManager();
        db.initTables();


        Map<String, ClientHandler> onlineMap = new ConcurrentHashMap<>();


        ExecutorService pool = Executors.newCachedThreadPool();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[Server] Listening on port " + PORT + " ...\n");


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Shutting down...");
            pool.shutdownNow();
            try { serverSocket.close(); } catch (IOException ignored) {}
        }));


        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New connection: " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket, onlineMap, db));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        }
    }
}
