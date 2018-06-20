package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Server {

    private static final int PORT = 9009;
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    private Server() {}

    public static User getUser(String username) {
        return USERS.get(username);
    }

    public static void addUser(String username, User user) {
        USERS.put(username, user);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            for (boolean isListening = true; isListening;) {
                new Thread(new UserThread(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.exit(-1);
        } finally {
            Database.close();
        }
    }
}