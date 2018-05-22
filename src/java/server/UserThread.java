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
                if (inputLine.startsWith("Nickname:")) {
                    String username = inputLine.replace("Nickname:", "@");
                    if (Server.recognizesUser(username)) {
                        user = Server.getUser(username);
                    } else {
                        user = new User(username);
                        Server.addUser(username, user);
                        System.out.println(username + " is registered");
                    }
                    Server.addConnection(username, out);
                    user.setOnline(true);
                    out.println("Successful connection, " + username);
                } else if (inputLine.equals("bye")) {
                    user.setOnline(false);
                    Server.removeConnection(user.getUsername());
                    break;
                } else if (inputLine.startsWith("@")) {
                    String interlocutorName = inputLine.substring(0, inputLine.indexOf(' '));
                    if (!Server.recognizesUser(interlocutorName)) {
                        out.println(interlocutorName + " has not been registered yet");
                    } else if (!Server.getUser(interlocutorName).isOnline()) {
                        out.println(interlocutorName + " is currently offline. Please try later");
                    } else {
                        String message = inputLine.substring(inputLine.indexOf(' ') + 1);
                        String detailedMessage = user.getUsername() + ": " + message;
                        Server.sendMessage(interlocutorName, detailedMessage);
                        out.println(user.getUsername() + ": " + inputLine);
                    }
                } else {
                    out.println("Unknown command: " + inputLine);
                }
            }
            System.out.println("Socket closed");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}