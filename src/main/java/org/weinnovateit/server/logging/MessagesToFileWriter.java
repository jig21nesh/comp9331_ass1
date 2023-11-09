package org.weinnovateit.server.logging;

import org.weinnovateit.server.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * This class is responsible for writing the messages to the file. It is a thread safe class since it has a lock.
 *
 */

public class MessagesToFileWriter extends CustomFileWriter{
    private static final String FILE_NAME = "messagelog";




    private static boolean isFileCreated = false;

    private static final Object fileLock = new Object();

    public MessagesToFileWriter(){
        if(!isFileCreated){
            isFileCreated = this.createFile(FILE_NAME);
        }

    }

    public void writeToFile(String toUsername, String message){
        int currentCounter = messageCounter.incrementAndGet();
        String formattedMessage = String.format("%d; %s; %s; %s", currentCounter, Config.dateFormat.format(new Date()), toUsername, message);
        BufferedWriter writer = null;
        synchronized (fileLock) {
            try{
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
