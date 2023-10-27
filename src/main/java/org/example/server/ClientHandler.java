package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private final CredentialValidator credentialValidator;

    private final Map<String, ActiveUser> activeUsersMap;

    private final LogMessages logMessages;

    private final BlockedUserManagement blockedUserManagement;

    public ClientHandler(Socket socket, CredentialValidator credentialValidator, Map<String, ActiveUser> activeUsersMap, BlockedUserManagement blockedUserManagement){
        this.socket = socket;
        this.credentialValidator = credentialValidator;
        this.activeUsersMap = activeUsersMap;
        this.blockedUserManagement = blockedUserManagement;

        logMessages = new LogMessages();
    }




    @Override
    public void run() {
        try{
            MessagePDU pdu = new MessagePDU();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            System.out.println(pdu.encodeString(MessageType.AUTH_MESSAGE, SystemMessages.AUTHENTICATION_MESSAGE));
            printWriter.println(SystemMessages.AUTHENTICATION_MESSAGE);


            String inputUsername = bufferedReader.readLine();
            System.out.println("Input username:: "+inputUsername);


            String inputPassword = bufferedReader.readLine();
            System.out.println("Input password:: "+inputPassword);

            boolean isValidUsername = credentialValidator.isValidUsername(inputUsername);
            boolean isValidPassword = credentialValidator.isValidPassword(inputUsername, inputPassword);
            boolean isBlocked = blockedUserManagement.isBlocked(inputUsername);

            boolean wasUsernameInvalid = false;
            int failedAttempts = 0;
            while(true){
                if(isValidUsername && isValidPassword && !isBlocked){
                    printWriter.println(SystemMessages.welcomeMessage(inputUsername));
                    printWriter.println(SystemMessages.commandList());
                    this.updateActiveUsers(socket, inputUsername);
                    logMessages.userOnline(inputUsername);
                    break;
                }else if(!isValidUsername){
                    printWriter.println(SystemMessages.invalidUsername(inputUsername));
                    inputUsername = bufferedReader.readLine();
                    isValidUsername = credentialValidator.isValidUsername(inputUsername);
                    wasUsernameInvalid = true;
                }else if(isBlocked){
                    printWriter.println(SystemMessages.blockedUserMessage());
                    this.blockedUserCleanup(socket);
                }
                else {
                    if(!wasUsernameInvalid)
                        printWriter.println(SystemMessages.invalidPassword());
                    else
                        printWriter.println(SystemMessages.VALID_USERNAME);
                    inputPassword = bufferedReader.readLine();
                    isValidPassword = credentialValidator.isValidPassword(inputUsername, inputPassword);
                    failedAttempts++;
                    if(failedAttempts == blockedUserManagement.getAllowedFailedAttempts()){
                        blockedUserManagement.addBlockedUser(inputUsername);
                        printWriter.println(SystemMessages.blockedMessage());
                        break;
                    }
                }
            }



            String clientInput;
            while ((clientInput = bufferedReader.readLine()) != null) {
                if(clientInput.startsWith("/msgto")){
                    MessageToProcessor processor = new MessageToProcessor(activeUsersMap, credentialValidator);
                    MessageToProcessor.MessageStatus status = processor.sendMessage(inputUsername, clientInput);
                    System.out.println("Status:: "+status.getStatusMessage());
                    if(status == MessageToProcessor.MessageStatus.SUCCESS){
                        printWriter.println("Success");
                        new LogMessages().messageTo(inputUsername, processor.getUsername(), processor.getMessage());
                    }else{
                        printWriter.println(status.getStatusMessage());
                    }

                }else if("/activeuser".equalsIgnoreCase(clientInput)) {
                    printWriter.println(this.getActiveUserInformation(inputUsername));
                    printWriter.println(SystemMessages.commandList());
                }else if("/logout".equalsIgnoreCase(clientInput)) {
                    this.logoutCleanUp(inputUsername);
                    printWriter.println(SystemMessages.logoutMessage(inputUsername));
                    logMessages.userOffline(inputUsername);
                }else{
                    printWriter.println("Invalid command");
                    printWriter.println(SystemMessages.commandList());
                }
            }

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }


    private void blockedUserCleanup(Socket socket){
        if(!socket.isClosed()){
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private synchronized void updateActiveUsers(Socket clientSocket, String username){
        ActiveUser activeUser = new ActiveUser(clientSocket, username, new Date());
        activeUsersMap.put(username, activeUser);
    }

    private void logoutCleanUp(String username){
        try{
            ActiveUser loggingOutUser = activeUsersMap.get(username);
            if(!loggingOutUser.getClientSocket().isClosed()){
                System.out.println("Input stream ::"+loggingOutUser.getClientSocket().getInputStream().available());
                loggingOutUser.getClientSocket().close();
            }
            activeUsersMap.remove(username);

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }

    }

    private String getActiveUserInformation(String username){
        StringBuilder stringBuilder = new StringBuilder();
        //System.out.println("Active user map size:: "+activeUsersMap.size());
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
                    stringBuilder.append(entry.getValue().getPort());
                    stringBuilder.append(" active since ");
                    stringBuilder.append(Config.dateFormat.format(entry.getValue().getLastActive()));
                    stringBuilder.append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }
}
