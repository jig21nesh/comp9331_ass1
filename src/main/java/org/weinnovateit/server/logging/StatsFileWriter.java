package org.weinnovateit.server.logging;

import org.weinnovateit.server.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class StatsFileWriter extends CustomFileWriter{
    private static final String FILE_NAME = "server_stats";

    private static boolean isFileCreated = false;

    private static final Object fileLock = new Object();

    public StatsFileWriter(){
        if(!isFileCreated){
            isFileCreated = this.createFile(FILE_NAME);
        }
    }
    public StatsFileWriter(boolean isLimitReached){
        if(isLimitReached){
            isFileCreated = this.createFile(FILE_NAME);
        }
    }
    public void writeToFile(String message){
        String formattedMessage = String.format("%s; %s", Config.dateFormat.format(new Date()), message);
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
