package org.example.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    InputValidator inputValidator;
    private static Map<String, String> credentailMap = new HashMap<>();

    int portNumber;
    int nofFailedAttempts;

    public Server(){
        inputValidator = new InputValidator();
    }

    private void createSocketAndWaitForConnection(){
        try{
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while(true){
                try {
                    Socket socket = serverSocket.accept();

                } catch (IOException e) {

                }
            }
        }catch (Exception exception){

        }
    }

    public void startServer(String[] inputValues){
        if(inputValidator.validatePort(inputValues[0]) && inputValidator.validateNumberOfAttempts(inputValues[1])) {
            portNumber = Integer.parseInt(inputValues[0]);
            nofFailedAttempts = Integer.parseInt(inputValues[1]);
            System.out.println(SystemMessages.successfulStartMessage(portNumber, nofFailedAttempts));
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
               credentialLoader.loadCredential(credentailMap);
           }catch( Exception exception){
               SystemMessages.failLoadingOfCredentails();
               System.exit(0);
           }

           new Server().startServer(args);
       }
    }
}
