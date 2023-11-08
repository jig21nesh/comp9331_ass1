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

    int portNumber;
    int nofFailedAttempts;

    private static final String CREDENTIALS_FILE = "credentials.txt";

    public Server(){
        this.initialiseResources();

    }

    private void printBanner() {
        String banner = "\n" +
                "*************************************************\n" +
                "*                SERVER STARTED                 *\n" +
                "*          Welcome to the Chat Server           *\n" +
                "*                                               *\n" +
                "*************************************************\n";
        System.out.println(banner);
    }
    private void initialiseResources(){
        inputValidator = new InputValidator();
        credentialValidator = new CredentialValidator(credentialMap);
        blockedUserManagement =  new BlockedUserManagement(nofFailedAttempts);
        new MessagesToFileWriter();
        new ActiveUsersFileWriter();
        new ServerStats(activeUsersMap);
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
        if(!inputValidator.validatePort(inputValues[0])){
            System.out.println("Invalid Port "+SystemMessages.portRangeMessage(Config.LOWEST_TCP_PORT, Config.HIGHEST_TCP_PORT));
            System.exit(0);
        }
        if(!inputValidator.validateNumberOfAttempts(inputValues[1])){
            System.out.println("Invalid number of failed attempts "+SystemMessages.USAGE_STRING);
            System.exit(0);
        }
        portNumber = Integer.parseInt(inputValues[0]);
        nofFailedAttempts = Integer.parseInt(inputValues[1]);

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

        System.out.println(SystemMessages.successfulStartMessage(portNumber, nofFailedAttempts));

        printBanner();
        this.createSocketAndWaitForConnection();

    }
    public static void main(String[] args) {
       if(args.length != 2){
           System.out.println(SystemMessages.USAGE_STRING);
           System.exit(0);
       }else{
           new Server().startServer(args);
       }
    }
}
