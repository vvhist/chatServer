package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 9009;

    private static Map<String, User>              users = new ConcurrentHashMap<>();

    public static User getUser(String username) {
        return users.get(username);
    }

    public static void addUser(String username, User user) {
        users.put(username, user);
    }

    public static boolean recognizesUser(String username) {
        return users.containsKey(username);
    }

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            for (boolean isListening = true; isListening;) {
                new Thread(new UserThread(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            System.err.println("Couldn't listen on port " + PORT);
            System.exit(-1);
        }
    }
}