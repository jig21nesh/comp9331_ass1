package org.example.server.logging;

import org.example.server.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagesToFileWriter extends CustomFileWriter{
    private static final String FILE_NAME = "messagelog";
    private static final AtomicInteger messageCounter = new AtomicInteger(0);
    private static final String FILE_EXTENSION = ".txt";


    private static boolean isFileCreated = false;

    private static final Object fileLock = new Object();

    public MessagesToFileWriter(){
        if(!isFileCreated){
            isFileCreated = this.createFile(FILE_NAME, FILE_EXTENSION);
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

}
