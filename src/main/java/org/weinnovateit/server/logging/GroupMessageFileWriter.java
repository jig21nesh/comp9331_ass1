package org.weinnovateit.server.logging;

import org.weinnovateit.server.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * This class is responsible for writing the group messages to the file. It is a thread safe class since it has a lock.
 *
 *
 */

public class GroupMessageFileWriter extends CustomFileWriter{

    private static final Object fileLock = new Object();

    private static final String FILE_NAME = "messagelog";
    private static boolean isFileCreated = false;

    private static String fileNameWithExtension;
    public GroupMessageFileWriter(String prefixGroupName){
        if(!isFileCreated){
            isFileCreated = this.createFile(prefixGroupName+"_"+FILE_NAME);
            fileNameWithExtension = prefixGroupName+"_"+FILE_NAME+FILE_EXTENSION;
        }
    }

    public void writeToFile(String currentUser, String message){
        String formattedMessage = String.format("%d; %s; %s; %s", messageCounter.incrementAndGet(), Config.dateFormat.format(new Date()), currentUser, message);
        BufferedWriter writer = null;
        synchronized (fileLock) {
            try{
                writer = new BufferedWriter(new FileWriter(fileNameWithExtension, true));
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
