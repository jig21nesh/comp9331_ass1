package org.example.server;

import org.example.server.logging.ActiveUsersFileWriter;
import org.example.server.logging.MessagesToFileWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    InputValidator inputValidator;

    CredentialValidator credentialValidator;

    BlockedUserManagement blockedUserManagement;
    private static final Map<String, String> credentialMap = new HashMap<>();

    private static final Map<String, ActiveUser> activeUsersMap = new HashMap<>();

    private static final MessagesToFileWriter fileWriter = new MessagesToFileWriter(); //TODO FIX THIS - it is not being used here.
    private static final ActiveUsersFileWriter ACTIVE_USERS_FILE_WRITER = new ActiveUsersFileWriter(); //TODO FIX THIS - it is not being used here.

    int portNumber;
    int nofFailedAttempts;

    private static final String CREDENTIALS_FILE = "credentials.txt";

    public Server(){
        inputValidator = new InputValidator();
        credentialValidator = new CredentialValidator(credentialMap);
        blockedUserManagement =  new BlockedUserManagement();
    }

    private void createSocketAndWaitForConnection(){
        try{
            ServerSocket serverSocket = new ServerSocket(portNumber);


            while(true){
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, credentialValidator, activeUsersMap, blockedUserManagement);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();

                } catch (SocketTimeoutException e) {
                    //e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }
    }

    public void startServer(String[] inputValues){
        if(inputValidator.validatePort(inputValues[0]) && inputValidator.validateNumberOfAttempts(inputValues[1])) {
            portNumber = Integer.parseInt(inputValues[0]);
            nofFailedAttempts = Integer.parseInt(inputValues[1]);
            System.out.println(SystemMessages.successfulStartMessage(portNumber, nofFailedAttempts));
            this.createSocketAndWaitForConnection();
        }else{
            System.out.println(SystemMessages.USAGE_STRING);
            System.exit(0);
        }
    }
    public static void main(String[] args) {
       if(args.length != 2){
           System.out.println(SystemMessages.USAGE_STRING);
           System.exit(0);
       }else{
           CredentialLoader credentialLoader = new CredentialLoader();
           try{
               boolean isLoaded = credentialLoader.loadCredential(credentialMap, CREDENTIALS_FILE);
               if(!isLoaded){
                   System.out.println(SystemMessages.failLoadingOfCredentials());
                   System.exit(0);
               }
           }catch( Exception exception){
               System.out.println(SystemMessages.failLoadingOfCredentials());
               System.exit(0);
           }

           new Server().startServer(args);
       }
    }
}
