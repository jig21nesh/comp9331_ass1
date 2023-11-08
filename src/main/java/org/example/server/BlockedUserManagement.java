package org.example.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static org.example.server.Config.BLOCK_STATUS_CHECK_IN_MILLISECONDS;

public class BlockedUserManagement implements Runnable{
    private static final HashMap<String, Long> blockedUsersList = new HashMap<>();

    private static final HashMap<String, Integer> failedAttemptTracker = new HashMap<>();
    private static volatile boolean checkerThreadCreated = false;

    public int getAllowedFailedAttempts() {
        return allowedFailedAttempts;
    }

    private int allowedFailedAttempts;

    Thread blockedUserCheckerthread = null;

    public BlockedUserManagement(){
        //allowedFailedAttempts = 3; // TODO get the value from console. This is a hard coded value for now.
    }

    public BlockedUserManagement(int allowedFailedAttempts){
        this.allowedFailedAttempts = allowedFailedAttempts;
    }

    public void increaseFailedAttemptCount(String username){
        if(failedAttemptTracker.containsKey(username)){
            int failedAttemptCount = failedAttemptTracker.get(username);
            failedAttemptCount++;
            failedAttemptTracker.put(username, failedAttemptCount);
        }else{
            failedAttemptTracker.put(username, 1);
        }

    }

    public int getFailedAttemptCount(String username){
        int count = 0;
        if(!failedAttemptTracker.containsKey(username)){
            this.increaseFailedAttemptCount(username);
        }
        count = failedAttemptTracker.get(username);


        return count;
    }

    public void removeFailedAttemptCount(String username){
        failedAttemptTracker.remove(username);
    }

    public boolean isBlocked(String username){
        return blockedUsersList.containsKey(username);
    }

    public void addBlockedUser(String username){
        long blockingTime = System.currentTimeMillis();
        blockedUsersList.put(username, blockingTime);


        if (!checkerThreadCreated) {
            blockedUserCheckerthread = new Thread(this);
            blockedUserCheckerthread.start();
            checkerThreadCreated = true;
        }
    }



    private void checkAndUpdateBlockedUsers() {
        long currentTimestamp = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = blockedUsersList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String username = entry.getKey();
            long blockTimestamp = entry.getValue();
            long elapsedSeconds = (currentTimestamp - blockTimestamp) / 1000;
            if (elapsedSeconds > Config.BLOCK_WAIT_TIME_IN_SECONDS) {
                iterator.remove();
                this.removeFailedAttemptCount(username);
            }
        }

        if (blockedUsersList.isEmpty() && blockedUserCheckerthread != null) {
            blockedUserCheckerthread.interrupt();
            blockedUserCheckerthread = null;
            checkerThreadCreated = false;
        }
    }

    @Override
    public void run() {
        try{
            while(true){
                this.checkAndUpdateBlockedUsers();
                Thread.sleep(BLOCK_STATUS_CHECK_IN_MILLISECONDS);
            }
        }catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }


}

