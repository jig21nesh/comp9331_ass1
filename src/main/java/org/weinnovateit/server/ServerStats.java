package org.weinnovateit.server;

import org.weinnovateit.server.commandprocessor.CommonGroupUtil;
import org.weinnovateit.server.logging.StatsFileWriter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * This class is responsible for printing server statistics to the file.
 * It is a separate thread which runs every 60 seconds. It prints the following statistics:
 * 1. Server configuration
 * 2. Number of active users
 * 3. Number of blocked users
 * 4. Number of groups
 * 5. Number of invited members
 * 6. Number of joined members
 * 7. Number of messages
 *
 *  Create by Jiggy (Jig2nesh@gmail.com)
 *
 */

public class ServerStats implements Runnable{
    private static boolean isThreadCreated = false;
    protected static final AtomicInteger statsCounter = new AtomicInteger(0);

    private final Map<String, ActiveUser> activeUsersMap;

    int portNumber;
    int nofFailedAttempts;

    public ServerStats(Map<String, ActiveUser> activeUsersMap, int portNumber, int nofFailedAttempts){
        this.activeUsersMap = activeUsersMap;
        this.portNumber = portNumber;
        this.nofFailedAttempts = nofFailedAttempts;
        if(!isThreadCreated){
            isThreadCreated = true;
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        StatsFileWriter statsFileWriter = new StatsFileWriter();
        while(true){
            try {
                Thread.sleep(60000);
                String sb = "" +
                        "Server Config :: Port " + portNumber +" No of failed attempts " + nofFailedAttempts + "\n" +
                        "Unblocking check cycle is " + Config.BLOCK_STATUS_CHECK_IN_MILLISECONDS + " milliseconds\n" +
                        "User unblock wait time is " + Config.BLOCK_WAIT_TIME_IN_SECONDS + " seconds\n" +
                        "Server Status:\n" + "Active Users " + activeUsersMap.size() + "\n" +
                        "Blocked Users " + new BlockedUserManagement().getNoOfBlockedUsers() + "\n" +
                        "No of groups " + CommonGroupUtil.getNoOfGroups() + "\n" +
                        "No of invited members "+ CommonGroupUtil.getTotalInvitedMembers() + "\n" +
                        "No of joined members "+ CommonGroupUtil.getTotalJoinedMembers() + "\n" +
                        "No of Messages including group messages " + StatsFileWriter.getMessageCounter() + "\n";
                statsFileWriter.writeToFile(sb);
                int count = statsCounter.incrementAndGet();
                if(count > 1000){
                    System.out.println("Server is running");
                    statsFileWriter = new StatsFileWriter(true);
                    statsCounter.set(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
