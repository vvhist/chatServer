package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public final class UserConnection implements Runnable {

    private final Socket socket;
    private PrintWriter out;
    private final UserOutputProtocol outputProtocol = new UserOutputProtocol(this);

    public UserConnection(Socket socket) {
        this.socket = socket;
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

            for (boolean listening = true; listening;) {
                String inputLine = in.readLine();
                if (!outputProtocol.process(inputLine)) {
                    listening = false;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            outputProtocol.close();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Command.Output command) {
        out.println(command);
    }

    public void send(Command.Output command, String output) {
        out.println(command + Command.DELIMITER + output);
    }
}