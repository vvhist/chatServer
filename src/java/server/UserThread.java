package server;

import java.io.*;
import java.net.Socket;
import java.time.ZoneId;
import java.util.Arrays;

public class UserThread implements Runnable {

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
                        inputLine = inputLine.replace("/log/", "");
                        String timeZone = inputLine.substring(0, inputLine.indexOf(' '));
                        String authData = inputLine.substring(   inputLine.indexOf(' ') + 1);
                        String username = authData.substring(0, authData.indexOf('/'));

                        if (Server.recognizesUser(username)) {
                            char[] password = authData.substring(authData.indexOf('/') + 1)
                                    .toCharArray();
                            User enteringUser = Server.getUser(username);
                            if (!enteringUser.hasCorrectPassword(password)) {
                                out.println("/deny");
                            } else {
                                out.println("/pass");
                                user = enteringUser;
                                user.connect(out, ZoneId.of(timeZone));
                            }
                        } else {
                            out.println("/deny");
                        }
                    } else if (inputLine.startsWith("/reg/")) {
                        inputLine = inputLine.replace("/reg/", "");
                        String timeZone = inputLine.substring(0, inputLine.indexOf(' '));
                        String authData = inputLine.substring(   inputLine.indexOf(' ') + 1);
                        String username = authData.substring(0, authData.indexOf('/'));

                        if (!Server.recognizesUser(username)) {
                            out.println("/pass");
                            char[] password = authData.substring(authData.indexOf('/') + 1)
                                    .toCharArray();
                            user = new User(username, password);
                            user.connect(out, ZoneId.of(timeZone));
                        } else {
                            out.println("/denyName");
                        }
                    } else if (inputLine.startsWith("/add/")) {
                        String contact = inputLine.replace("/add/", "");
                        if (!Server.recognizesUser(contact)) {
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
        } catch (IOException e) {
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