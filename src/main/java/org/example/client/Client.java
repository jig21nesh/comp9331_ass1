package org.example.client;

import org.example.client.p2ptransfer.CommandProcessor;
import org.example.client.p2ptransfer.Receiver;
import org.example.client.p2ptransfer.Sender;

import java.io.*;
import java.net.*;

public class Client {

    private boolean validateInput(String ipAddress, String port){
        InputValidator inputValidator = new InputValidator();
        return (inputValidator.validateServerIpAddress(ipAddress) && inputValidator.isValidPort(port));
    }

    private void startClient(String ipAddress, String port, String udpPort){
        Socket socket = null;
        BufferedReader bufferedReaderFromSocket = null;
        PrintWriter writeToServer = null;
        BufferedReader localInputReader = null;

        try{
            MessageProcessor processor = new MessageProcessor();

            socket = new Socket(ipAddress, Integer.parseInt(port));

            bufferedReaderFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writeToServer = new PrintWriter(socket.getOutputStream(), true);
            localInputReader = new BufferedReader(new InputStreamReader(System.in));

            String loginMessage = bufferedReaderFromSocket.readLine();
            System.out.println(processor.getPrompt(loginMessage));

            System.out.print(MessageProcessor.MessageType.USERNAME.getPrompt());
            String usernameText = localInputReader.readLine();
            writeToServer.println(processor.encodeString(MessageProcessor.MessageType.USERNAME, usernameText));

            System.out.print(MessageProcessor.MessageType.PASSWORD.getPrompt());
            String passwordText = localInputReader.readLine();
            writeToServer.println(processor.encodeString(MessageProcessor.MessageType.PASSWORD, passwordText));



            ServerMessageReaderThread runnableObject = new ServerMessageReaderThread(bufferedReaderFromSocket, socket);
            Thread messageReader = new Thread(runnableObject);
            messageReader.start();
            this.waitAndProcessInput(socket, writeToServer, localInputReader, runnableObject, udpPort);
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

    private void waitAndProcessInput(Socket serverSocket, PrintWriter writeToServer, BufferedReader localInputReader, ServerMessageReaderThread thread, String udpPort) throws Exception{
        boolean keepRunning = true;
        String userInput = null;
        MessageProcessor processor = new MessageProcessor();
        String encodedString;
        Receiver udpReceiver = null;
        while(keepRunning){
            Thread.sleep(100);
            ClientState currentState = thread.getCurrentState();

            switch(currentState){
                case BLOCKED:
                    keepRunning = false;
                    serverSocket.close();
                    break;
                case INVALID_PASSWORD:
                    System.out.print("Password: ");
                    userInput = localInputReader.readLine();
                    encodedString = processor.encodeString(MessageProcessor.MessageType.PASSWORD, userInput);
                    writeToServer.println(encodedString);
                    break;
                case INVALID_USERNAME:
                    System.out.print("Username: ");
                    userInput = localInputReader.readLine();
                    encodedString = processor.encodeString(MessageProcessor.MessageType.USERNAME, userInput);
                    writeToServer.println(encodedString);
                    break;
                case LOGIN_SUCCESSFUL:
                    encodedString = processor.encodeString(MessageProcessor.MessageType.UDP_PORT, udpPort);
                    udpReceiver = new Receiver(udpPort);
                    writeToServer.println(encodedString);
                    break;
                case LOGGED_IN_USER:
                    userInput = localInputReader.readLine();
                    if(userInput.startsWith("/p2pvideo")){
                        CommandProcessor p2pCommandProcessor = new CommandProcessor();
                        CommandProcessor.ProcessorStatus status = p2pCommandProcessor.validateCommand(userInput);
                        if(status != CommandProcessor.ProcessorStatus.SUCCESS){
                            System.out.println(status.getMessage());
                        }else{
                            encodedString = processor.encodeString(MessageProcessor.MessageType.COMMAND, "/fetch "+p2pCommandProcessor.getOnlineUsername(userInput));
                            writeToServer.println(encodedString);

                            Thread.sleep(100);

                            Sender sender = new Sender(thread.getAuthenticatedUser(), p2pCommandProcessor.getFileName(userInput));
                            String udpDetails = thread.getToUserUdpDetails();
                            String ipAddress = udpDetails.split(" ")[0];
                            String port = udpDetails.split(" ")[1];
                            boolean isSent = sender.send(ipAddress, port);
                            if(isSent)
                                System.out.println(thread.getCommandList());
                            else
                                System.out.println("Error.. ");
                        }
                    }else{
                        encodedString = processor.encodeString(MessageProcessor.MessageType.COMMAND, userInput);
                        writeToServer.println(encodedString);
                    }

                    break;
                default:
                    break;
            }


            if ("/logout".equalsIgnoreCase(userInput)) {
                if(!thread.isLogoutConfirmationReceived()){
                    Thread.sleep(100); //TODO - Find a better way to handle this
                }
                System.out.println("Logging out... "+udpReceiver);
                if(udpReceiver != null)
                    udpReceiver.setHasUserLoggedOut(true);

                serverSocket.close();
                keepRunning = false;
                System.exit(0);
            }

        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        if(args.length != 3){
            System.out.println("Usage: java Client <server ip> <server port>");
            System.exit(0);
        }else{
            if(client.validateInput(args[0], args[1])){
                String serverIpAddress = args[0];
                String serverPort = args[1];
                String udpPort = args[2];
                client.startClient(serverIpAddress, serverPort, udpPort);
            }else{
                System.out.println("Usage: java Client <server ip> <server port>");
                System.exit(0);
            }
        }
    }
}


