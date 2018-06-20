package server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.ZoneId;

public final class UserThread implements Runnable {

    private Socket socket;
    private User user = null;

    public UserThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("UserThread: " + Thread.currentThread().getName());
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

                        user = Server.getUser(username);
                        user.connect(out, ZoneId.of(timeZone));
                    } else if (inputLine.startsWith("/reg/")) {
                        inputLine = inputLine.replace("/reg/", "");

                        String timeZone = inputLine.substring(0, inputLine.indexOf(' '));
                        String authData = inputLine.substring(   inputLine.indexOf(' ') + 1);
                        String username     = authData.substring(0, authData.indexOf('/'));
                        String passwordHash = authData.substring(   authData.indexOf('/') + 1);

                        if (!Database.containsUser(username)) {
                            out.println("/allowReg");
                            Database.addUser(username, passwordHash);
                            user = new User(username);
                            user.connect(out, ZoneId.of(timeZone));
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
                    String contactName = inputLine.substring(0, inputLine.indexOf('/'));
                    String message     = inputLine.substring(   inputLine.indexOf('/') + 1);

                    User contact = Server.getUser(contactName);
                    String detailedMessage = user.getUsername() + ": " + message;

                    contact.sendMessage(user.getUsername() + "/" + detailedMessage);
                    user.sendMessage(contact.getUsername() + "/" + detailedMessage);
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (user != null) {
                user.disconnect();
            }
            try {
                socket.close();
                System.out.println("Socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}