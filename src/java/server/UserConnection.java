package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public final class UserConnection implements Runnable {

    private final Socket socket;
    private final UserOutputProtocol outputProtocol = new UserOutputProtocol();

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

            outputProtocol.setWriter(out);

            for (boolean listening = true; listening;) {
                String inputLine = in.readLine();
                if (!outputProtocol.process(inputLine)) {
                    listening = false;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            String username = outputProtocol.getUsername();
            if (username != null) {
                Server.remove(username);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}