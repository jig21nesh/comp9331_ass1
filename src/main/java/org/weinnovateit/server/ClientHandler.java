package org.weinnovateit.server;

import org.weinnovateit.server.commandprocessor.*;
import org.weinnovateit.server.logging.ActiveUsersFileWriter;
import org.weinnovateit.server.logging.ConsoleMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

/**
 * ClientHandler class is responsible for handling the client connection. Implementation is based on a simple state machine.
 *
 * It is responsible for:
 * 1. Authenticating the user
 * 2. Handling the client commands
 * 3. Logging the messages
 * 4. Updating the active users map
 * 5. Blocking the user
 * Created by Jiggy (jig2nesh@gmail.com)
 *
 */

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

                            /**
                             * MSGTO and MSG_CONTENT is to differentiate between the content to be displayed on the client side.
                             * TODO - Refactor this to a better approach and removed the msgto_content
                             */

                            if (status == MessageTo.MessageStatus.SUCCESS) {
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.MSGTO, status.getStatusMessage()));
                                new ConsoleMessages().messageTo(inputUsername, processor.getUsername(), processor.getMessage());
                            } else {
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.MSGTO_CONTENT, status.getStatusMessage()));
                            }
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));

                        }else if(clientInput.startsWith("/groupmsg")){
                            GroupMessage groupMessageProcessor = new GroupMessage(activeUsersMap);
                            GroupMessage.GroupStatus groupMessageStatus = groupMessageProcessor.sendMessage(inputUsername, clientInput);
                            logMessages.commandLogMessage(inputUsername, "/groupmsg");
                            switch (groupMessageStatus){
                                case USER_NOT_JOINED_GROUP:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.GROUP_MSG, SystemMessages.userNotJoinedGroup(inputUsername, groupMessageProcessor.getGroupNameFromCommand(clientInput))));
                                    logMessages.commandReturnMessage(SystemMessages.userNotJoinedGroup(inputUsername, groupMessageProcessor.getGroupNameFromCommand(clientInput)));
                                    break;
                                case USER_NOT_INVITED:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.GROUP_MSG, SystemMessages.userNotInvitedToGroup(inputUsername, groupMessageProcessor.getGroupNameFromCommand(clientInput))));
                                    logMessages.commandReturnMessage(SystemMessages.userNotInvitedToGroup(inputUsername, groupMessageProcessor.getGroupNameFromCommand(clientInput)));
                                    break;
                                case SUCCESS:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.GROUP_MSG, groupMessageStatus.getMessage()));
                                    logMessages.commandReturnMessage(SystemMessages.groupSuccessMessage(inputUsername, groupMessageProcessor.getGroupNameFromCommand(clientInput),groupMessageProcessor.getFormattedMessageWithoutGroup()));
                                    break;
                                default:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.GROUP_MSG, groupMessageStatus.getMessage()));
                                    break;

                            }
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        }else if(clientInput.startsWith("/fetch")){
                            logMessages.commandLogMessage(inputUsername, "/fetch");
                            Fetch fetch = new Fetch(activeUsersMap);
                            Fetch.FetchStatus result = fetch.getIpAndUdpPort(fetch.getToUsername(clientInput));
                            if(result == Fetch.FetchStatus.SUCCESS){
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.FETCH, fetch.getUdpServerDetails()));
                                logMessages.commandReturnMessage("/fetch return details "+fetch.getUdpServerDetails());
                            }else{
                                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.FETCH_ERROR, result.getMessage()));
                                logMessages.commandReturnMessage(result.getMessage());
                            }
                        }else if ("/activeuser".equalsIgnoreCase(clientInput)) {
                            logMessages.commandLogMessage(inputUsername, clientInput, true);
                            ActiveUsers activeUsersProcessor = new ActiveUsers(activeUsersMap);
                            String returnMessage = activeUsersProcessor.getActiveUsers(inputUsername);
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.ACTIVE_USERS, returnMessage));
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                            logMessages.commandReturnMessage(returnMessage);
                        }else if(clientInput.startsWith("/creategroup")){
                            logMessages.commandLogMessage(inputUsername, "/creategroup");
                            CreateGroup createGroupProcessor = new CreateGroup(activeUsersMap, credentialValidator);
                            CreateGroup.GroupStatus groupStatus = createGroupProcessor.createGroup(inputUsername, clientInput);

                            switch (groupStatus){
                                case SUCCESS:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, createGroupProcessor.getVerboseMessage(inputUsername)));
                                    logMessages.commandReturnMessage(createGroupProcessor.getVerboseMessage(inputUsername));
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
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.CREATE_GROUP, groupStatus.getMessage()));
                                    logMessages.commandReturnMessage(groupStatus.getMessage());
                                    break;
                            }
                            printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                        }

                        else if(clientInput.startsWith("/joingroup")){
                            logMessages.commandLogMessage(inputUsername, "/joingroup");
                            JoinGroup joinGroupProcessor = new JoinGroup();
                            JoinGroup.GroupStatus joinGroupStatus = joinGroupProcessor.joinGroup(inputUsername, clientInput);
                            switch (joinGroupStatus) {
                                case SUCCESS:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.JOIN_GROUP, joinGroupProcessor.getVerboseMessage(clientInput, false)));
                                    logMessages.commandReturnMessage(joinGroupProcessor.getVerboseMessage(clientInput, true));
                                    break;
                                case OWNER_CANNOT_JOIN_GROUP:
                                    String t = SystemMessages.ownerCannotJoinTheGroup(inputUsername, joinGroupProcessor.getGroupNameFromCommand(clientInput));
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.JOIN_GROUP, t));
                                    logMessages.commandReturnMessage(t);
                                default:
                                    printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.JOIN_GROUP, joinGroupStatus.getMessage()));
                                    logMessages.commandReturnMessage(joinGroupStatus.getMessage());
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
        new ActiveUsersFileWriter().writeToFile(activeUser);
    }

    private boolean hasActiveUser(String username){
        return activeUsersMap.containsKey(username);
    }




}
