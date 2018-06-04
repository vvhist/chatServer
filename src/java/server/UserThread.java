package server;

import java.io.*;
import java.net.Socket;

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
                        String authData = inputLine.replace("/log/", "");
                        String username = authData.substring(0, authData.indexOf('/'));
                        if (!Server.recognizesUser(username)) {
                            out.println("/deny");
                        } else {
                            user = Server.getUser(username);
                            if (!user.hasCorrectPassword(authData.substring(
                                    authData.indexOf('/') + 1).toCharArray())) {
                                user = null;
                                out.println("/deny");
                            } else {
                                Server.addConnection(username, out);
                                user.setOnline(true);
                                out.println("/pass");
                            }
                        }
                    } else if (inputLine.startsWith("/reg/")) {
                        String authData = inputLine.replace("/reg/", "");
                        String username = authData.substring(0, authData.indexOf('/'));
                        if (Server.recognizesUser(username)) {
                            out.println("/denyName");
                        } else {
                            user = new User(username, authData.substring(
                                                      authData.indexOf('/') + 1).toCharArray());
                            Server.addUser(username, user);
                            Server.addConnection(username, out);
                            user.setOnline(true);
                            out.println("/pass");
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
                    String interlocutorName = inputLine.substring(0, inputLine.indexOf('/'));
                    if (!Server.getUser(interlocutorName).isOnline()) {
                        out.println(interlocutorName + "/"
                                  + interlocutorName + " is currently offline. Please try later.");
                    } else {
                        String message = inputLine.substring(inputLine.indexOf('/') + 1);
                        String detailedMessage = user.getUsername() + "/"
                                               + user.getUsername() + ": " + message;
                        Server.sendMessage(interlocutorName, detailedMessage);
                        out.println(interlocutorName + "/" + user.getUsername() + ": " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (user != null) {
                user.setOnline(false);
                Server.removeConnection(user.getUsername());
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