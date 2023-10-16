package org.example.client;

import java.io.*;
import java.net.*;

public class Client {

    private boolean validateInput(String ipAddress, String port){
        InputValidator inputValidator = new InputValidator();
        return (inputValidator.validateServerIpAddress(ipAddress) && inputValidator.isValidPort(port));
    }

    private void startClient(String ipAddress, String port){
        Socket socket = null;
        BufferedReader bufferedReaderFromSocket = null;
        PrintWriter writeToServer = null;
        BufferedReader localInputReader = null;

        try{
            socket = new Socket(ipAddress, Integer.parseInt(port));

            bufferedReaderFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writeToServer = new PrintWriter(socket.getOutputStream(), true);
            localInputReader = new BufferedReader(new InputStreamReader(System.in));

            String loginMessage = bufferedReaderFromSocket.readLine();
            System.out.println(loginMessage);

            System.out.print("Username: ");
            String username = localInputReader.readLine();
            writeToServer.println(username);

            System.out.print("Password: ");
            String password = localInputReader.readLine();
            writeToServer.println(password);



            ServerMessageReaderThread runnableObject = new ServerMessageReaderThread(bufferedReaderFromSocket, socket);
            Thread messageReader = new Thread(runnableObject);
            messageReader.start();
            this.waitAndProcessInput(socket, writeToServer, localInputReader, runnableObject);
            messageReader.join();


        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }finally {
            try{
                if (localInputReader != null) {
                    localInputReader.close();
                }
                if (writeToServer != null) {
                    writeToServer.close();
                }
                if (bufferedReaderFromSocket != null) {
                    bufferedReaderFromSocket.close();
                }
                if(socket != null && !socket.isClosed()){
                    socket.close();
                }
            }catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        }
    }

    private void waitAndProcessInput(Socket serverSocket, PrintWriter writeToServer, BufferedReader localInputReader, ServerMessageReaderThread thread) throws Exception{
        boolean keepRunning = true;
        String userInput;
        while(keepRunning){
            userInput = localInputReader.readLine();
            writeToServer.println(userInput);
            if ("/logout".equalsIgnoreCase(userInput)) {
                if(!thread.isLogoutConfirmationReceived()){
                    Thread.sleep(100); //TODO - Find a better way to handle this
                }
                keepRunning = false;
                serverSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        if(args.length != 2){
            System.out.println("Usage: java Client <server ip> <server port>");
            System.exit(0);
        }else{
            if(client.validateInput(args[0], args[1])){
                String serverIpAddress = args[0];
                String serverPort = args[1];
                client.startClient(serverIpAddress, serverPort);
            }else{
                System.out.println("Usage: java Client <server ip> <server port>");
                System.exit(0);
            }
        }
    }
}


