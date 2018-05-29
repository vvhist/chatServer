package server;

import java.io.*;
import java.net.Socket;

public class UserThread implements Runnable {

    private Socket socket;
    private User user;

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
                if (inputLine.startsWith("name ")) {
                    String username = inputLine.replace("name ", "");
                    if (Server.recognizesUser(username)) {
                        user = Server.getUser(username);
                    } else {
                        user = new User(username);
                        Server.addUser(username, user);
                        System.out.println(username + " is registered");
                    }
                    Server.addConnection(username, out);
                    user.setOnline(true);
                    out.println("Server Server: Successful connection, " + username
                            + ". Use command 'add <nickname>' to start a conversation.");
                } else if (inputLine.startsWith("Server ")) {
                    String message = inputLine.replace("Server ", "");
                    out.println("Server " + user.getUsername() + ": " + message);
                    if (message.startsWith("add ")) {
                        String interlocutorName = message.replace("add ", "");
                        if (!Server.recognizesUser(interlocutorName)) {
                            out.println("Server Server: "+ interlocutorName + " is not found.");
                        } else {
                            out.println("newChat " + interlocutorName);
                        }
                    } else if (message.equals("bye")) {
                        user.setOnline(false);
                        Server.removeConnection(user.getUsername());
                        break;
                    } else {
                        out.println("Server Server: Unknown command: " + message);
                    }
                } else {
                    String interlocutorName = inputLine.substring(0, inputLine.indexOf(' '));
                    if (!Server.recognizesUser(interlocutorName)) {
                        out.println("Server Server: " + interlocutorName + " is not found.");
                    } else if (!Server.getUser(interlocutorName).isOnline()) {
                        out.println(interlocutorName + " "
                                  + interlocutorName + " is currently offline. Please try later.");
                    } else {
                        String message = inputLine.substring(inputLine.indexOf(' ') + 1);
                        String detailedMessage = user.getUsername() + " "
                                               + user.getUsername() + ": " + message;
                        Server.sendMessage(interlocutorName, detailedMessage);
                        out.println(interlocutorName + " " + user.getUsername() + ": " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                user.setOnline(false);
                Server.removeConnection(user.getUsername());
                System.out.println("Socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}