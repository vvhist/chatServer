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
                            User enteringUser = Server.getUser(username);
                            if (!enteringUser.hasCorrectPassword(
                                    authData.substring(authData.indexOf('/') + 1).toCharArray())) {
                                out.println("/deny");
                            } else {
                                out.println("/pass");
                                user = enteringUser;
                                user.connect(out);
                            }
                        }
                    } else if (inputLine.startsWith("/reg/")) {
                        String authData = inputLine.replace("/reg/", "");
                        String username = authData.substring(0, authData.indexOf('/'));
                        if (Server.recognizesUser(username)) {
                            out.println("/denyName");
                        } else {
                            out.println("/pass");
                            user = new User(username, authData.substring(
                                                      authData.indexOf('/') + 1).toCharArray());
                            Server.addUser(username, user);
                            user.connect(out);
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
                    out.println(     contact.getUsername() + "/" + detailedMessage);
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