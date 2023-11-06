package org.example.server.logging;

import org.example.server.ActiveUser;
import org.example.server.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ActiveUsersFireWriter {
    private static final String FILE_NAME = "userlog";
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final AtomicInteger messageCounter = new AtomicInteger(0);
    private static final String FILE_EXTENSION = ".txt";

    private static boolean isFileCreated = false;

    public ActiveUsersFireWriter(){
        if(!isFileCreated){
            this.createFile();
        }
    }


    private int getLastSequenceNumber() throws IOException {
        readWriteLock.readLock().lock();
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_NAME + FILE_EXTENSION));
            if (!lines.isEmpty()) {
                String lastLine = lines.get(lines.size() - 1);
                String[] parts = lastLine.split("; ");
                return Integer.parseInt(parts[0].trim()); // Return the sequence number
            }
            return 0;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void removeUser(ActiveUser activeUser){
        readWriteLock.writeLock().lock();
        try {
            Path path = Paths.get(FILE_NAME+FILE_EXTENSION);
            List<String> lines = Files.readAllLines(path);
            List<String> updatedLines = new ArrayList<>();

            int newSequenceNumber = 1;
            for (String line : lines) {
                System.out.println("Line: " + line);
                String[] parts = line.split("; ");
                System.out.println(" Condition :: "+(parts.length > 4 && !parts[2].trim().equals(activeUser.getUsername()) && !parts[3].trim().equals(activeUser.getIpAddress()) && Integer.parseInt(parts[4].trim()) != activeUser.getPort()));
                System.out.println("Parts ::"+parts.length);
                System.out.println("Parts 2 ::"+parts[2]+"  username "+activeUser.getUsername());
                System.out.println("Parts 3 ::"+parts[3]+"  IP "+activeUser.getIpAddress());
                System.out.println("Parts 4 ::"+parts[4]+"  UDP "+activeUser.getUdpPort());

                if (!parts[2].trim().equals(activeUser.getUsername())) {
                    updatedLines.add(newSequenceNumber + "; " + parts[1] + "; " + parts[2] + "; " + parts[3]+"; " + parts[4]);
                    newSequenceNumber++;
                }
            }

            Files.write(path, updatedLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void writeToFile(ActiveUser user){
        readWriteLock.writeLock().lock();
        try {
            String userLogEntry = String.format("%d; %s; %s; %s; %s%n",
                    this.getLastSequenceNumber()+1,
                    Config.dateFormat.format(user.getLastActive()),
                    user.getUsername(),
                    user.getIpAddress(),
                    user.getUdpPort());
            Files.write(Paths.get(FILE_NAME+FILE_EXTENSION), userLogEntry.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            readWriteLock.writeLock().unlock();
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
        File file = new File(FILE_NAME+FILE_EXTENSION);
        String newFileName = FILE_NAME+"_" + Config.logFileBackupDateFormat.format(new Date()) + FILE_EXTENSION;
        file.delete();
    }




}
