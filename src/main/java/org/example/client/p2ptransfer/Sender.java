package org.example.client.p2ptransfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender {
    private final String currentUser;
    private final String fileName;

    public Sender(String currentUser, String fileName) {
        this.currentUser = currentUser;
        this.fileName = fileName;
    }

    private String getFileNameMetaData(String fileName){
        return this.currentUser+"_"+fileName;
    }

    public boolean send(String ipAddress, String port){
        boolean isSent = false;
        try {
            int BUFFER_SIZE = 1024;
            DatagramSocket socket = new DatagramSocket();

            File file = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            byte[] nameBytes = this.getFileNameMetaData(file.getName()).getBytes();

            DatagramPacket packetForFileNameMetaData = new DatagramPacket(nameBytes, nameBytes.length, InetAddress.getByName(ipAddress), Integer.parseInt(port));
            socket.send(packetForFileNameMetaData);

            byte[] fileChunks = new byte[BUFFER_SIZE];
            int bytesRead;

            // Send file data
            while ((bytesRead = bufferedInputStream.read(fileChunks)) != -1) {
                DatagramPacket sendPacket = new DatagramPacket(fileChunks, bytesRead, InetAddress.getByName(ipAddress), Integer.parseInt(port));
                socket.send(sendPacket);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Server thread interrupted during sleep.");
                }
            }

            byte[] eofIndicatorPacket = "EOF".getBytes();
            DatagramPacket endOfFilePacket = new DatagramPacket(eofIndicatorPacket, eofIndicatorPacket.length, InetAddress.getByName(ipAddress), Integer.parseInt(port));
            socket.send(endOfFilePacket);

            System.out.println("File " + fileName + " has been sent.");
            isSent = true;
        }catch (Exception exception){
            exception.printStackTrace();
            System.out.println("Unable to send file to "+ipAddress+":"+port);
        }
        return isSent;

    }
}
