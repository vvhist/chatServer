package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Server {

    private static final int PORT = 9009;
    private static final Map<String, OnlineUser> USERS = new ConcurrentHashMap<>();

    private Server() {}

    public static OnlineUser getUser(String username) {
        return USERS.get(username);
    }

    public static void connect(OnlineUser user) {
        USERS.put(user.getUsername(), user);
    }

    public static void disconnect(OnlineUser user) {
        USERS.remove(user.getUsername());
    }

    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    pool.submit((new UserThread(serverSocket.accept())));
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