package org.example.server;

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

    int portNumber;
    int nofFailedAttempts;

    public Server(){
        inputValidator = new InputValidator();
        credentialValidator = new CredentialValidator(credentialMap);
        blockedUserManagement =  new BlockedUserManagement();
    }

    private void createSocketAndWaitForConnection(){
        try{
            ServerSocket serverSocket = new ServerSocket(portNumber);
            //serverSocket.setSoTimeout(Config.SOCKET_ACCEPT_TIMEOUT);

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
               credentialLoader.loadCredential(credentialMap);
           }catch( Exception exception){
               System.out.println(SystemMessages.failLoadingOfCredentials());
               System.exit(0);
           }

           new Server().startServer(args);
       }
    }
}
