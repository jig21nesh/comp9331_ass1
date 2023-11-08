package org.example.server;

import org.example.server.commandprocessor.CommonGroupUtil;
import org.example.server.logging.StatsFileWriter;

import java.util.Map;

public class ServerStats implements Runnable{
    private static boolean isThreadCreated = false;

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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
