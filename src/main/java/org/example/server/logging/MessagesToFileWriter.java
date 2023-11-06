package org.example.server.logging;

import org.example.server.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagesToFileWriter {
    private static final String FILE_NAME = "messagelog";
    private static final AtomicInteger messageCounter = new AtomicInteger(0);
    private static final String FILE_EXTENSION = ".txt";


    private static boolean isFileCreated = false;

    private static final Object fileLock = new Object();

    public MessagesToFileWriter(){
        if(!isFileCreated){
            this.createFile();
        }
    }

    public void writeToFile(String toUsername, String message){
        int currentCounter = messageCounter.incrementAndGet();
        String formattedMessage = String.format("%d; %s; %s; %s", currentCounter, Config.dateFormat.format(new Date()), toUsername, message);
        BufferedWriter writer = null;
        synchronized (fileLock) {
            try{
                System.out.println("Writing this message :::: "+formattedMessage);
                writer = new BufferedWriter(new FileWriter(FILE_NAME+FILE_EXTENSION, true));
                writer.write(formattedMessage + "\n");
                writer.flush();
            }catch (Exception exception){
                System.err.println("Failed to write to the message log file.");
            }finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.err.println("Failed to close the writer.");
                    }
                }
            }
        }
    }

    private void createFile(){
        File messageLogFile = new File(FILE_NAME+FILE_EXTENSION);
        if(messageLogFile.exists()){
            this.backupCurrentFile();
        }
        try{
            isFileCreated = messageLogFile.createNewFile();
        }catch (Exception exception){
            System.out.println("Failed to create message log file.");
        }
    }

    private void backupCurrentFile(){
        File messageLogFile = new File(FILE_NAME+FILE_EXTENSION);
        String newFileName = FILE_NAME+"_" + Config.logFileBackupDateFormat.format(new Date()) + FILE_EXTENSION;
        messageLogFile.delete();
        if (messageLogFile.renameTo(new File(newFileName))) {
            System.out.println("Renamed existing message log file to " + newFileName);
        } else {
            System.err.println("Failed to rename the existing message log file.");
        }
    }
}
