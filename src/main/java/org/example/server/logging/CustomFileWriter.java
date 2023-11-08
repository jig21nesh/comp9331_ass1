package org.example.server.logging;

import org.example.server.Config;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomFileWriter {

    protected static final AtomicInteger messageCounter = new AtomicInteger(0);
    protected static final String FILE_EXTENSION = ".txt";

    protected boolean createFile(String fileName){
        boolean isFileCreated = false;
        File messageLogFile = new File(fileName+FILE_EXTENSION);
        if(messageLogFile.exists()){
            this.backupCurrentFile(fileName);
        }
        try{
            isFileCreated = messageLogFile.createNewFile();
        }catch (Exception exception){
            System.out.println("Failed to create message log file.");
        }
        return isFileCreated;
    }

    public static int getMessageCounter(){
        return messageCounter.get();
    }

    protected void backupCurrentFile(String fileName){
        File messageLogFile = new File(fileName+FILE_EXTENSION);
        String newFileName = fileName+"_" + Config.logFileBackupDateFormat.format(new Date()) + FILE_EXTENSION;
        if (messageLogFile.renameTo(new File(newFileName))) {
            System.out.println("Renamed existing message log file to " + newFileName);
        } else {
            System.err.println("Failed to rename the existing message log file.");
        }
    }
}
