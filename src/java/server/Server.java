package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, User>              users = new ConcurrentHashMap<>();
    private static Map<String, PrintWriter> connections = new ConcurrentHashMap<>();

    public static User getUser(String username) {
        return users.get(username);
    }

    public static void addUser(String username, User user) {
        users.put(username, user);
    }

    public static boolean recognizesUser(String username) {
        return users.containsKey(username);
    }

    public static void addConnection(String username, PrintWriter out) {
        connections.put(username, out);
    }

    public static void removeConnection(String username) {
        connections.remove(username);
    }

    public static void sendMessage(String username, String message) {
        connections.get(username).println(message);
    }

    public static void main(String[] args) {
        System.out.println("Server: " + Thread.currentThread().getName());
        if (args.length != 1) {
            System.err.println("Usage: java chat/Server <port number>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            for (boolean isListening = true; isListening;) {
                new Thread(new UserThread(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            System.err.println("Couldn't listen on port " + port);
            System.exit(-1);
        }
    }
}