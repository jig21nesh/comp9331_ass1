package org.example.server.logging;

import org.example.server.Config;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomFileWriter {

    protected static final AtomicInteger messageCounter = new AtomicInteger(0);
    protected static final String FILE_EXTENSION = ".txt";

    protected boolean createFile(String fileName, String fileExtension){
        boolean isFileCreated = false;
        File messageLogFile = new File(fileName+fileExtension);
        if(messageLogFile.exists()){
            this.backupCurrentFile(fileName, fileExtension);
        }
        try{
            isFileCreated = messageLogFile.createNewFile();
        }catch (Exception exception){
            System.out.println("Failed to create message log file.");
        }
        return isFileCreated;
    }

    protected void backupCurrentFile(String fileName, String fileExtension){
        File messageLogFile = new File(fileName+fileExtension);
        String newFileName = fileName+"_" + Config.logFileBackupDateFormat.format(new Date()) + fileExtension;
        messageLogFile.delete();
        if (messageLogFile.renameTo(new File(newFileName))) {
            System.out.println("Renamed existing message log file to " + newFileName);
        } else {
            System.err.println("Failed to rename the existing message log file.");
        }
    }
}
