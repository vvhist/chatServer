package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Server {

    private static final int PORT = 9009;
    private static final Map<String, UserOutputProtocol> OUTPUTS = new ConcurrentHashMap<>();

    private Server() {}

    public static void sendTo(String username, Message message) {
        UserOutputProtocol userOutput = OUTPUTS.get(username);
        if (userOutput != null) {
            userOutput.sendMessage(message);
        }
    }

    public static void add(String username, UserOutputProtocol userOutput) {
        OUTPUTS.put(username, userOutput);
    }

    public static void remove(String username) {
        OUTPUTS.remove(username);
    }

    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    pool.submit((new UserConnection(serverSocket.accept())));
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port " + PORT);
                System.exit(-1);
            } finally {
                Database.close();
            }
        });
    }
}