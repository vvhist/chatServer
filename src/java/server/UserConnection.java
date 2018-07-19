package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public final class UserConnection implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConnection.class);
    private final Socket socket;
    private PrintWriter out;
    private final UserOutputProtocol outputProtocol = new UserOutputProtocol(this);

    public UserConnection(Socket socket) {
        this.socket = socket;
        LOGGER.debug("New accepted connection");
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(new BufferedWriter(
                     new OutputStreamWriter(
                             socket.getOutputStream(), "UTF-8")), true)) {
            this.out = out;
            LOGGER.debug("Connection has been established");

            for (boolean listening = true; listening;) {
                String inputLine = in.readLine();

                if (!outputProtocol.process(inputLine)) {
                    listening = false;
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to establish connection", e);
        } finally {
            outputProtocol.close();
            try {
                socket.close();
                LOGGER.debug("Connection has been closed");
            } catch (IOException e) {
                LOGGER.warn("Failed to close a connection", e);
            }
        }
    }

    public void send(Command.Output command) {
        out.println(command);
        LOGGER.trace("From server: {}", command.name());
    }

    public void send(Command.Output command, String output) {
        out.println(command + Command.DELIMITER + output);
        LOGGER.trace("From server: {} {}", command.name(), output);
    }
}