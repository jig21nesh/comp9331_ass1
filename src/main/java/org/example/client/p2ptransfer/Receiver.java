package org.example.client.p2ptransfer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class Receiver implements Runnable{

    private static final int BUFFER_SIZE = 1024;

    private Thread udpReceiver = null;
    private String udpPort;

    private static boolean isReceiverThreadCreated = false;

    private static Map<String, FileOutputStream> fileOutputStreamMap = new HashMap<>();
    private static Map<String, String> fileNameMap = new HashMap<>();

    public void setHasUserLoggedOut(boolean hasUserLoggedOut) {
        this.hasUserLoggedOut = hasUserLoggedOut;
    }

    private boolean hasUserLoggedOut = false;

    public void setOnline(boolean online) {
        isOnline = online;

    }

    private boolean isOnline;



    public Receiver(String udpPort) {
        this.udpPort = udpPort;
        if(!isReceiverThreadCreated){
            udpReceiver = new Thread(this);
            udpReceiver.start();
            isReceiverThreadCreated = true;
        }

    }


    @Override
    public void run() {
        DatagramSocket datagramSocket = null;
        try{
            datagramSocket = new DatagramSocket(Integer.parseInt(this.udpPort));
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);

            System.out.println("UDP Client is running and waiting for files... hasUserLoggedOut "+hasUserLoggedOut);

            while(!hasUserLoggedOut){
                datagramSocket.receive(datagramPacket);
                String senderKey = datagramPacket.getAddress()+":"+datagramPacket.getPort();

                String eofIndicator = new String(datagramPacket.getData(), 0, 3);


                if ("EOF".equals(eofIndicator.trim())) {
                    FileOutputStream fos = fileOutputStreamMap.get(senderKey);
                    if (fos != null) {
                        fos.close();
                        fileOutputStreamMap.remove(senderKey);
                        System.out.println("File transfer completed from " + senderKey);
                    }
                    fileNameMap.remove(senderKey); // Clean up
                } else {
                    // If file name for this sender is not set, this packet contains the file name
                    if (!fileNameMap.containsKey(senderKey)) {
                        String fileName = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).trim();
                        fileNameMap.put(senderKey, fileName);
                        FileOutputStream fos = new FileOutputStream(fileName);
                        fileOutputStreamMap.put(senderKey, fos);
                    } else {
                        // Write data to file for the current sender
                        FileOutputStream fos = fileOutputStreamMap.get(senderKey);
                        if (fos != null) {
                            fos.write(datagramPacket.getData(), 0, datagramPacket.getLength());
                        }
                    }
                }
            }
        }catch (Exception e){
            udpReceiver.interrupt();
            System.out.println("Unable to create UDP socket"+e.getMessage());
        }finally {
            System.out.println("Shutting down UDP receiver .. ");
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
            // Close all remaining FileOutputStream objects
            for (FileOutputStream fos : fileOutputStreamMap.values()) {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        System.out.println("Error while closing streams");
                    }
                }
            }
        }
    }
}
