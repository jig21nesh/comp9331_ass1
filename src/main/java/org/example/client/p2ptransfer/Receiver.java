package org.example.client.p2ptransfer;

import org.example.client.ServerMessageReaderThread;

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



    private static Map<String, FileOutputStream> fileOutputStreamMap = new HashMap<>();
    private static Map<String, String> fileNameMap = new HashMap<>();




    private ServerMessageReaderThread serverMessageReaderThread;




    public Receiver(String udpPort, ServerMessageReaderThread thread) {
        this.serverMessageReaderThread = thread;
        this.udpPort = udpPort;
        udpReceiver = new Thread(this);
        udpReceiver.start();

    }


    private volatile boolean running = true;
    private DatagramSocket datagramSocket = null;

    public void shutdown() {
        running = false;
        udpReceiver.interrupt();
    }

    @Override
    public void run() {

        try{
            datagramSocket = new DatagramSocket(Integer.parseInt(this.udpPort));
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);


            while(running && !Thread.currentThread().isInterrupted()){
                datagramSocket.receive(datagramPacket);
                String senderKey = datagramPacket.getAddress()+":"+datagramPacket.getPort();

                String eofIndicator = new String(datagramPacket.getData(), 0, 3);


                if ("EOF".equals(eofIndicator.trim())) {
                    FileOutputStream fos = fileOutputStreamMap.get(senderKey);
                    if (fos != null) {
                        fos.close();
                        fileOutputStreamMap.remove(senderKey);
                        String fileName = fileNameMap.get(senderKey);
                        System.out.printf("Received %s from %s%n", fileName.split("_")[1], fileName.split("_")[0]);
                        System.out.println(serverMessageReaderThread.getCommandList());
                    }
                    fileNameMap.remove(senderKey);
                } else {
                    if (!fileNameMap.containsKey(senderKey)) {
                        String fileName = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).trim();
                        fileNameMap.put(senderKey, fileName);
                        FileOutputStream fos = new FileOutputStream(fileName);
                        fileOutputStreamMap.put(senderKey, fos);
                    } else {
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
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
            for (Map.Entry<String, FileOutputStream> entry : fileOutputStreamMap.entrySet()) {
                try {
                    if (entry.getValue() != null) {
                        entry.getValue().close();
                    }
                } catch (IOException e) {
                    System.out.println("Error while closing file output stream: " + e.getMessage());
                }
            }
            System.out.println("UDP Receiver shutdown complete.");
        }
    }
}
