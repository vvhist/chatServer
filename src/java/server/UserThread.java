package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class UserThread implements Runnable {

    private final Socket socket;
    private OnlineUser user = null;

    public UserThread(Socket socket) {
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

            for (String inputLine; (inputLine = in.readLine()) != null;) {
                if (inputLine.startsWith("/")) {
                    if (inputLine.equals("/exit")) {
                        break;
                    } else if (inputLine.startsWith("/log/")) {
                        String username = inputLine.replace("/log/", "");

                        if (Database.containsUser(username)) {
                            out.println("/hash/" + Database.getPasswordHash(username));
                        } else {
                            out.println("/denyLog");
                        }
                    } else if (inputLine.startsWith("/match/")) {
                        inputLine = inputLine.replace("/match/", "");

                        String username = inputLine.substring(0, inputLine.indexOf('/'));
                        String timeZone = inputLine.substring(   inputLine.indexOf('/') + 1);

                        user = new OnlineUser(username, ZoneId.of(timeZone), out);
                        Server.connect(user);
                    } else if (inputLine.startsWith("/reg/")) {
                        inputLine = inputLine.replace("/reg/", "");

                        String timeZone = inputLine.substring(0, inputLine.indexOf(' '));
                        String authData = inputLine.substring(   inputLine.indexOf(' ') + 1);
                        String username     = authData.substring(0, authData.indexOf('/'));
                        String passwordHash = authData.substring(   authData.indexOf('/') + 1);

                        if (Database.addUser(username, passwordHash)) {
                            out.println("/allowReg");
                            user = new OnlineUser(username, ZoneId.of(timeZone), out);
                            Server.connect(user);
                        } else {
                            out.println("/denyReg");
                        }
                    } else if (inputLine.startsWith("/add/")) {
                        String contact = inputLine.replace("/add/", "");

                        if (!Database.containsUser(contact)) {
                            out.println("/notFound/" + contact);
                        } else {
                            out.println("/newDialog/" + contact);
                        }
                    }
                } else {
                    String recipientName = inputLine.substring(0, inputLine.indexOf('/'));
                    String text          = inputLine.substring(   inputLine.indexOf('/') + 1);

                    String senderName = user.getUsername();
                    LocalDateTime serverTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

                    Message message = new Message(senderName, recipientName, serverTime, text);
                    Database.addMessage(message);

                    OnlineUser recipient = Server.getUser(recipientName);
                    user.sendMessage(message);
                    recipient.sendMessage(message);
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (user != null) {
                Server.disconnect(user);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}