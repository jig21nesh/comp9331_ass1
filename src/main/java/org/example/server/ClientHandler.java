package org.example.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final CredentialValidator credentialValidator;

    private final Map<String, ActiveUser> activeUsersMap;

    public ClientHandler(Socket socket, CredentialValidator credentialValidator, Map<String, ActiveUser> activeUsersMap){
        this.socket = socket;
        this.credentialValidator = credentialValidator;
        this.activeUsersMap = activeUsersMap;
    }


    private void messageTo(){
        System.out.println("Managing message to command");
    }

    @Override
    public void run() {
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println(SystemMessages.AUTHENTICATION_MESSAGE);

            String inputUsername = bufferedReader.readLine();
            System.out.println("Input username:: "+inputUsername);


            String inputPassword = bufferedReader.readLine();
            System.out.println("Input password:: "+inputPassword);

            boolean isValidUsername = credentialValidator.isValidUsername(inputUsername);
            boolean isValidPassword = credentialValidator.isValidPassword(inputUsername, inputPassword);


            if(isValidUsername && isValidPassword){
                printWriter.println(SystemMessages.welcomeMessage(inputUsername));
                printWriter.println(SystemMessages.commandList());
                this.updateActiveUsers(inputUsername, socket.getInetAddress().getHostAddress(), socket.getPort());
            }else if(!isValidUsername){
                printWriter.println(SystemMessages.invalidUsername(inputUsername));
            }else {
                printWriter.println(SystemMessages.invalidPassword(0));
            }
            String clientInput;
            while ((clientInput = bufferedReader.readLine()) != null) {
                if ("/msgto".startsWith(clientInput)) {
                    MessageToProcessor processor = new MessageToProcessor(activeUsersMap);
                    System.out.println(processor.sendMessage(clientInput).message);
                    printWriter.println("Message Received!");
                }else if("/activeuser".equalsIgnoreCase(clientInput)) {
                    printWriter.println(this.getActiveUserInformation(inputUsername));
                    printWriter.println(SystemMessages.commandList());
                }else if("/logout".equalsIgnoreCase(clientInput)) {
                    this.logoutCleanUp(inputUsername);
                    printWriter.println(SystemMessages.logoutMessage(inputUsername));
                }else{
                    printWriter.println("Invalid command");
                    printWriter.println(SystemMessages.commandList());
                }
            }

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }


    private synchronized void updateActiveUsers(String username, String ipAddress, int portNumber){
        ActiveUser activeUser = new ActiveUser(username, new Date(), ipAddress, portNumber);
        activeUsersMap.put(username, activeUser);
    }

    private void logoutCleanUp(String username){
        activeUsersMap.remove(username);
    }

    private String getActiveUserInformation(String username){
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        System.out.println("Active user map size:: "+activeUsersMap.size());
        System.out.println("Username requesting the information :: "+username);
        if(activeUsersMap.containsKey(username) && activeUsersMap.size() == 1) {
            stringBuilder.append("no other active users");
            stringBuilder.append("\n");
        }else{
            for (Map.Entry<String, ActiveUser> entry : activeUsersMap.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().toString());
                if(!entry.getKey().equals(username)){
                    stringBuilder.append(entry.getValue().getUsername());
                    stringBuilder.append(" ");
                    stringBuilder.append(entry.getValue().getIpAddress());
                    stringBuilder.append(" ");
                    stringBuilder.append(entry.getValue().getPortNumber());
                    stringBuilder.append(" active since ");
                    stringBuilder.append(dateFormat.format(entry.getValue().getLastActive()));
                    stringBuilder.append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }
}
