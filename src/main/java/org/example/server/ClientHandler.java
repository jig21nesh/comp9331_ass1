package org.example.server;

import org.example.server.commandprocessor.ActiveUsers;
import org.example.server.commandprocessor.MessageTo;

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
            MessageProcessor messageProcessor = new MessageProcessor();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.AUTH_PROMPT, MessageProcessor.MessageType.AUTH_PROMPT.getPrompt()));


            String encodedUserName = bufferedReader.readLine();
            String inputUsername = messageProcessor.getContent(encodedUserName);
            System.out.println("Input username:: "+inputUsername);


            String encodedPassword = bufferedReader.readLine();
            String inputPassword = messageProcessor.getContent(encodedPassword);
            System.out.println("Input password:: "+inputPassword);

            boolean isValidUsername = credentialValidator.isValidUsername(inputUsername);
            boolean isValidPassword = credentialValidator.isValidPassword(inputUsername, inputPassword);
            boolean isBlocked = blockedUserManagement.isBlocked(inputUsername);

            boolean wasUsernameInvalid = false;
            int failedAttempts = 0;
            while(true){
                if(isValidUsername && isValidPassword && !isBlocked){

                    if(this.hasActiveUser(inputUsername)){
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.ALREADY_LOGGED_USER, SystemMessages.userAlreadyLoggedIn()));
                        this.blockedUserCleanup(socket);
                    }else{
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.WELCOME_MESSAGE, SystemMessages.welcomeMessage(inputUsername)));
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        this.updateActiveUsers(socket, inputUsername);
                        logMessages.userOnline(inputUsername);
                    }
                    break;
                }else if(!isValidUsername){
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.INVALID_USERNAME, SystemMessages.invalidUsername(inputUsername)));
                    inputUsername = messageProcessor.getContent(bufferedReader.readLine());
                    isValidUsername = credentialValidator.isValidUsername(inputUsername);
                    wasUsernameInvalid = true;
                }else if(isBlocked){
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.BLOCKED_USER, SystemMessages.blockedUserMessage()));
                    this.blockedUserCleanup(socket);
                    break;
                }
                else {

                    if(this.hasActiveUser(inputUsername)){
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.ALREADY_LOGGED_USER, SystemMessages.userAlreadyLoggedIn()));
                        this.blockedUserCleanup(socket);
                        break;
                    }

                    if(!wasUsernameInvalid)
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.INVALID_PASSWORD, SystemMessages.invalidPassword()));
                    else{
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.VALID_USERNAME, SystemMessages.VALID_USERNAME));
                        wasUsernameInvalid = false;
                    }

                    inputPassword =  messageProcessor.getContent(bufferedReader.readLine());
                    isValidPassword = credentialValidator.isValidPassword(inputUsername, inputPassword);
                    failedAttempts++;
                    if(failedAttempts == blockedUserManagement.getAllowedFailedAttempts()){
                        blockedUserManagement.addBlockedUser(inputUsername);
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.BLOCKING_USER, SystemMessages.blockingUserMessage()));
                        break;
                    }
                }
            }



            String encodedClientInput;
            while ((encodedClientInput = bufferedReader.readLine()) != null) {
                String clientInput = messageProcessor.getContent(encodedClientInput);
                System.out.println("Client input:: "+clientInput+" encoded message "+encodedClientInput);
                if(clientInput.startsWith("/msgto")){
                    MessageTo processor = new MessageTo(activeUsersMap, credentialValidator);
                    MessageTo.MessageStatus status = processor.sendMessage(inputUsername, clientInput);
                    System.out.println("Status:: "+status.getStatusMessage());
                    if(status == MessageTo.MessageStatus.SUCCESS){
                        printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.MSGTO, status.getStatusMessage()));
                        new LogMessages().messageTo(inputUsername, processor.getUsername(), processor.getMessage());
                    }else{
                       printWriter.println( messageProcessor.encodeString(MessageProcessor.MessageType.MSGTO_CONTENT, status.getStatusMessage()));
                    }
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.COMMAND_LIST, SystemMessages.commandList()));

                }else if("/activeuser".equalsIgnoreCase(clientInput)) {
                    System.out.println("Fetching details");
                    ActiveUsers activeUsersProcessor = new ActiveUsers(activeUsersMap);
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.ACTIVE_USERS, activeUsersProcessor.getActiveUsers(inputUsername)));
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                }else if("/logout".equalsIgnoreCase(clientInput)) {
                    System.out.println("Logging out  "+inputUsername);
                    printWriter.println(messageProcessor.encodeString(MessageProcessor.MessageType.LOGOUT, SystemMessages.logoutMessage(inputUsername)));
                    this.logoutCleanUp(inputUsername);
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

    private boolean hasActiveUser(String username){
        return activeUsersMap.containsKey(username);
    }

    private void logoutCleanUp(String username){
        try{
            ActiveUser loggingOutUser = activeUsersMap.get(username);
            if(!loggingOutUser.getClientSocket().isClosed()){
                Thread.sleep(100);
                loggingOutUser.getClientSocket().close();
            }
            activeUsersMap.remove(username);

        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }

    }


}
