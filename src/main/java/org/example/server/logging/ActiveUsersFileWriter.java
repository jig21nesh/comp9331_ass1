package org.example.server.logging;

import org.example.server.ActiveUser;
import org.example.server.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ActiveUsersFileWriter extends CustomFileWriter{
    private static final String FILE_NAME = "userlog";
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    private static boolean isFileCreated = false;

    public ActiveUsersFileWriter(){
        if(!isFileCreated){
            isFileCreated = this.createFile(FILE_NAME,FILE_EXTENSION);
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
                String[] parts = line.split("; ");
                if (!parts[2].trim().equals(activeUser.getUsername())) {
                    updatedLines.add(newSequenceNumber + "; " + parts[1] + "; " + parts[2] + "; " + parts[3]+"; " + parts[4]);
                    newSequenceNumber++;
                }
            }

            Files.write(path, updatedLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println(e.getMessage());
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






}
