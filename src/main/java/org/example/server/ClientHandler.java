package org.example.server;

import org.example.server.commandprocessor.*;
import org.example.server.logging.ActiveUsersFireWriter;
import org.example.server.logging.ConsoleMessages;

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

    private final ConsoleMessages logMessages;

    private final BlockedUserManagement blockedUserManagement;

    private enum ClientState{

        PROMPT,
        AUTHENTICATION,
        INVALID_USERNAME,

        VALID_USERNAME,

        INVALID_PASSWORD,
        LOGGED_IN,
        BLOCKED,
        ALREADY_LOGGED_IN,
        WAIT_FOR_UDP_PORT,
        LOGGED_OUT
    }

    public ClientHandler(Socket socket, CredentialValidator credentialValidator, Map<String, ActiveUser> activeUsersMap, BlockedUserManagement blockedUserManagement){
        this.socket = socket;
        this.credentialValidator = credentialValidator;
        this.activeUsersMap = activeUsersMap;
        this.blockedUserManagement = blockedUserManagement;

        logMessages = new ConsoleMessages();
    }




    @Override
    public void run() {
        try{
            MessageTranslator messageProcessor = new MessageTranslator();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            ClientState currentState = ClientState.PROMPT;
            String inputUsername = null;
            String inputPassword = null;
            boolean isValidUsername = false;
            boolean isValidPassword = false;
            boolean wasUsernameInvalid = false;
            String udpPort = null;

            while (currentState != ClientState.LOGGED_OUT){
                switch (currentState){
                    case PROMPT:
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.AUTH_PROMPT, MessageTranslator.MessageType.AUTH_PROMPT.getPrompt()));

                        String encodedUserName = bufferedReader.readLine();
                        inputUsername = messageProcessor.getContent(encodedUserName);

                        String encodedPassword = bufferedReader.readLine();
                        inputPassword = messageProcessor.getContent(encodedPassword);


                        currentState = ClientState.AUTHENTICATION;
                        break;
                    case AUTHENTICATION:
                        ClientState authStatus = this.handleAuthentication(inputUsername, inputPassword);
                        switch (authStatus) {
                            case LOGGED_IN:
                                blockedUserManagement.removeFailedAttemptCount(inputUsername);
                                logMessages.userOnline(inputUsername);
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.WELCOME_MESSAGE, SystemMessages.welcomeMessage(inputUsername)));
                                currentState = ClientState.WAIT_FOR_UDP_PORT;
                                break;
                            case BLOCKED:
                                currentState = ClientState.BLOCKED;
                                break;
                            case ALREADY_LOGGED_IN:
                                currentState = ClientState.ALREADY_LOGGED_IN;
                                break;
                            case INVALID_USERNAME:
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.INVALID_USERNAME, SystemMessages.invalidUsername(inputUsername, true)));
                                currentState = ClientState.INVALID_USERNAME;
                                break;
                            case INVALID_PASSWORD:
                                blockedUserManagement.increaseFailedAttemptCount(inputUsername);
                                currentState = ClientState.INVALID_PASSWORD;
                                break;
                        }
                        break;
                    case INVALID_USERNAME:

                        while(!isValidUsername){
                            inputUsername = messageProcessor.getContent(bufferedReader.readLine());
                            ClientState usernameStatus = this.handleUsername(inputUsername);
                            if(usernameStatus == ClientState.INVALID_USERNAME){
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.INVALID_USERNAME, SystemMessages.invalidUsername(inputUsername, true)));
                                wasUsernameInvalid = true;
                            }else{
                                currentState = usernameStatus;
                                isValidUsername = true;
                            }
                        }
                        break;

                    case VALID_USERNAME:
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.VALID_USERNAME, SystemMessages.VALID_USERNAME));
                        inputPassword = messageProcessor.getContent(bufferedReader.readLine());
                        currentState = this.handleAuthentication(inputUsername, inputPassword);
                        break;
                    case INVALID_PASSWORD:
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.INVALID_PASSWORD, SystemMessages.invalidPassword()));
                        inputPassword = messageProcessor.getContent(bufferedReader.readLine());
                        if(blockedUserManagement.getFailedAttemptCount(inputUsername) >= blockedUserManagement.getAllowedFailedAttempts()){
                            blockedUserManagement.addBlockedUser(inputUsername);
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.BLOCKING_USER, SystemMessages.blockingUserMessage()));
                            currentState = ClientState.BLOCKED;
                        }else{
                            currentState = this.handleAuthentication(inputUsername, inputPassword);
                            if(currentState == ClientState.INVALID_PASSWORD)
                                blockedUserManagement.increaseFailedAttemptCount(inputUsername);
                            if(currentState == ClientState.LOGGED_IN){
                                blockedUserManagement.removeFailedAttemptCount(inputUsername);
                                logMessages.userOnline(inputUsername);
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.WELCOME_MESSAGE, SystemMessages.welcomeMessage(inputUsername)));
                                currentState = ClientState.WAIT_FOR_UDP_PORT;
                            }
                        }
                        break;

                    case WAIT_FOR_UDP_PORT:
                        udpPort = messageProcessor.getContent(bufferedReader.readLine());
                        this.updateActiveUsers(socket, inputUsername, udpPort);
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        currentState = ClientState.LOGGED_IN;
                        break;

                    case LOGGED_IN:
                        String encodedClientInput = bufferedReader.readLine();
                        String clientInput = messageProcessor.getContent(encodedClientInput);
                        if (clientInput.startsWith("/msgto")) {
                            MessageTo processor = new MessageTo(activeUsersMap, credentialValidator);
                            MessageTo.MessageStatus status = processor.sendMessage(inputUsername, clientInput);
                            if (status == MessageTo.MessageStatus.SUCCESS) {
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.MSGTO, status.getStatusMessage()));
                                new ConsoleMessages().messageTo(inputUsername, processor.getUsername(), processor.getMessage());
                            } else {
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.MSGTO_CONTENT, status.getStatusMessage()));
                            }
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));

                        } else if ("/activeuser".equalsIgnoreCase(clientInput)) {
                            logMessages.commandLogMessage(inputUsername, clientInput, true);
                            ActiveUsers activeUsersProcessor = new ActiveUsers(activeUsersMap);
                            String returnMessage = activeUsersProcessor.getActiveUsers(inputUsername);
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.ACTIVE_USERS, returnMessage));
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                            logMessages.commandReturnMessage(returnMessage);
                        }else if(clientInput.startsWith("/creategroup")){
                            logMessages.commandLogMessage(inputUsername, "/creategroup");
                            CreateGroup createGroupProcessor = new CreateGroup(activeUsersMap, credentialValidator);
                            CreateGroup.GroupCreationStatus groupCreationStatus = createGroupProcessor.createGroup(inputUsername, clientInput);
                            switch (groupCreationStatus){
                                case SUCCESS:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, createGroupProcessor.getVerboseMessage()));
                                    logMessages.commandReturnMessage(createGroupProcessor.getVerboseMessage());
                                    break;
                                case INVALID_USERNAME:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, SystemMessages.invalidUserWhileCreatingGroup(createGroupProcessor.getProblemUser())));
                                    logMessages.commandReturnMessage(SystemMessages.invalidUserWhileCreatingGroup(createGroupProcessor.getProblemUser()));
                                    break;
                                case USER_NOT_ONLINE:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, SystemMessages.userIsNotOnline(createGroupProcessor.getProblemUser())));
                                    logMessages.commandReturnMessage(SystemMessages.userIsNotOnline(createGroupProcessor.getProblemUser()));
                                    break;
                                case CURRENT_USER_PRESENT_IN_MEMBERS_LIST:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, SystemMessages.currentUserPresentInMembersList(inputUsername)));
                                    logMessages.commandReturnMessage(SystemMessages.currentUserPresentInMembersList(inputUsername));
                                    break;
                                case ALREADY_EXISTS:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, SystemMessages.groupAlreadyExists(createGroupProcessor.getGroupName())));
                                    logMessages.commandReturnMessage(SystemMessages.groupAlreadyExists(createGroupProcessor.getGroupName()));
                                    break;
                                default:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, groupCreationStatus.getMessage()));
                                    logMessages.commandReturnMessage(groupCreationStatus.getMessage());
                                    break;
                            }
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        }

                        else if ("/logout".equalsIgnoreCase(clientInput)) {
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.LOGOUT, SystemMessages.logoutMessage(inputUsername)));
                            new Logout(activeUsersMap).logoutCleanUp(inputUsername);
                            logMessages.userOffline(inputUsername);
                            currentState = ClientState.LOGGED_OUT;
                        } else {
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.INVALID_COMMAND, SystemMessages.invalidCommand()));
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        }
                        break;
                    case BLOCKED:
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.BLOCKED_USER, SystemMessages.blockedUserMessage()));
                        this.blockedUserCleanup(socket);
                        currentState = ClientState.LOGGED_OUT;
                        break;
                    case ALREADY_LOGGED_IN:
                        printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.ALREADY_LOGGED_USER, SystemMessages.userAlreadyLoggedIn()));
                        this.blockedUserCleanup(socket);
                        currentState = ClientState.LOGGED_OUT;
                        break;

                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
            System.out.println(exception.getMessage());
        }
    }

    private ClientState handleUsername(String username){
        boolean isValidUsername = credentialValidator.isValidUsername(username);
        ClientState authStatus = ClientState.VALID_USERNAME;

        if(isValidUsername){
            if(blockedUserManagement.isBlocked(username)){
                authStatus = ClientState.BLOCKED;
            }else if(this.hasActiveUser(username)){
                authStatus = ClientState.ALREADY_LOGGED_IN;
            }
        }else{
            authStatus = ClientState.INVALID_USERNAME;
        }
        return authStatus;
    }


    private ClientState handleAuthentication(String username, String password){
        boolean isValidUsername = credentialValidator.isValidUsername(username);
        boolean isValidPassword = credentialValidator.isValidPassword(username, password);
        ClientState authStatus = ClientState.LOGGED_IN;

        if(isValidUsername){
            if(blockedUserManagement.isBlocked(username)){
                authStatus = ClientState.BLOCKED;
            }else if(this.hasActiveUser(username)){
                authStatus = ClientState.ALREADY_LOGGED_IN;
            }else if(!isValidPassword){
                authStatus = ClientState.INVALID_PASSWORD;
            }
        }else{
            authStatus = ClientState.INVALID_USERNAME;
        }
        return authStatus;

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
    private synchronized void updateActiveUsers(Socket clientSocket, String username, String udpPort){
        ActiveUser activeUser = new ActiveUser(clientSocket, username, new Date(), udpPort);
        activeUsersMap.put(username, activeUser);
        new ActiveUsersFireWriter().writeToFile(activeUser);
    }

    private boolean hasActiveUser(String username){
        return activeUsersMap.containsKey(username);
    }




}
