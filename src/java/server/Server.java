package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 9009;
    private static final Map<String, UserOutputProtocol> OUTPUTS = new ConcurrentHashMap<>();

    private Server() {}

    public static void sendTo(String username, Message message) {
        UserOutputProtocol userOutput = OUTPUTS.get(username);
        if (userOutput != null) {
            userOutput.sendMessage(message);
        }
    }

    public static void addOnlineUser(String username, UserOutputProtocol userOutput) {
        OUTPUTS.put(username, userOutput);
        LOGGER.info("User {} is online", username);
    }

    public static void removeOnlineUser(String username) {
        OUTPUTS.remove(username);
        LOGGER.info("User {} is offline", username);
    }

    public static void main(String[] args) {
        LOGGER.info("Application started");

        Executors.newSingleThreadExecutor().submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                LOGGER.info("Server is listening on port {}", PORT);

                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    pool.submit((new UserConnection(serverSocket.accept())));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to listen on port {}", PORT, e);
                System.exit(-1);
            } finally {
                Database.close();
            }
        });
    }
}