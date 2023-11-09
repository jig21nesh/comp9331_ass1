package org.weinnovateit.client.p2ptransfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * This class is responsible for sending the files to other users. It does the following:
 *
 * 1. Opens the UDP connection
 * 2. Reads the file from the disk
 * 3. Sends the file to the user
 * 4. Closes the UDP connection
 *
 */

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

    /**
     *
     * This method is responsible for sending the file to the user. It does the following:
     * 1. Opens the UDP connection
     * 2. Reads the file from the disk
     * 3. Sends the file name
     * 4. Sends file to the receiver
     * 5. Sends EOF indicator
     * 6. Closes the UDP connection
     *
     * @param toUser
     * @param ipAddress
     * @param port
     * @return
     */

    public boolean send(String toUser, String ipAddress, String port){
        boolean isSent = false;
        try {
            DatagramSocket socket = new DatagramSocket();

            long startTime = System.currentTimeMillis();

            File file = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            byte[] nameBytes = this.getFileNameMetaData(file.getName()).getBytes();

            DatagramPacket packetForFileNameMetaData = new DatagramPacket(nameBytes, nameBytes.length, InetAddress.getByName(ipAddress), Integer.parseInt(port));
            socket.send(packetForFileNameMetaData);

            byte[] fileChunks = new byte[UDPConfig.BUFFER_SIZE];
            int bytesRead;
            int totalChunks = Math.toIntExact(file.length() / UDPConfig.BUFFER_SIZE);
            int sequenceNumber = 0;
            while ((bytesRead = bufferedInputStream.read(fileChunks)) != -1) {
                DatagramPacket sendPacket = new DatagramPacket(fileChunks, bytesRead, InetAddress.getByName(ipAddress), Integer.parseInt(port));
                socket.send(sendPacket);
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Server thread interrupted during sleep.");
                }
                sequenceNumber++;
                double progress = (double) sequenceNumber / totalChunks * 100;
                long elapsedTime = System.currentTimeMillis() - startTime;

                String elapsedTimeStr = getFormattedTimer(elapsedTime);

                System.out.print("Progress: " + String.format("%.2f", progress) + "% | Transfer Time: " + elapsedTimeStr + "\r");
                System.out.flush();
            }

            byte[] eofIndicatorPacket = "EOF".getBytes();
            DatagramPacket endOfFilePacket = new DatagramPacket(eofIndicatorPacket, eofIndicatorPacket.length, InetAddress.getByName(ipAddress), Integer.parseInt(port));
            socket.send(endOfFilePacket);

            System.out.println("File " + fileName + " has been uploaded.");
            isSent = true;
        }catch (Exception exception){
            System.out.println(exception.getMessage()+"  Unable to send file"+fileName+" to "+toUser+" on "+ipAddress+":"+port);

        }
        return isSent;

    }

    /**
     *
     *
     * This method is responsible for formatting the elapsed time in a human readable format.
     *
     * @param elapsedTime
     * @return
     */

    private String getFormattedTimer(long elapsedTime) {
        String elapsedTimeStr;
        if (elapsedTime < 60000) {
            elapsedTimeStr = elapsedTime / 1000 + "s";
        } else if (elapsedTime < 3600000) {
            long minutes = (elapsedTime / 1000) / 60;
            long seconds = (elapsedTime / 1000) % 60;
            elapsedTimeStr = String.format("%02d:%02d", minutes, seconds);
        } else {
            long hours = (elapsedTime / 1000) / 3600;
            long minutes = ((elapsedTime / 1000) % 3600) / 60;
            long seconds = (elapsedTime / 1000) % 60;
            elapsedTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return elapsedTimeStr;
    }
}
