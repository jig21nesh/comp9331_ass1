package org.example.server;

import java.util.Arrays;

public class MessageParser {
    public static void main(String[] args) {
        String test = "/msgto test hello how are you?";
        new MessageParser().parseText(test);
    }

    private void parseText(String input){
        String[] parts = input.split(" ");

        if (parts.length >= 3 && "/msgto".equals(parts[0])) {
            String command = parts[0];
            String username = parts[1];
            String message = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

            System.out.println("Command: " + command);
            System.out.println("Username: " + username);
            System.out.println("Message: " + message);
        } else {
            System.out.println("Invalid input format.");
        }
    }
}
