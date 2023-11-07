package org.example.client.p2ptransfer;

public class CommandProcessor {

    public enum ProcessorStatus{
        INVALID_COMMAND("Invalid command /p2pvideo <Online Username> <File Name>, Please try again."),
        SUCCESS("Success");

        public String getMessage() {
            return message;
        }

        private String message;

        ProcessorStatus(String message) {
            this.message = message;
        }
    }
    public ProcessorStatus validateCommand(String command){
        ProcessorStatus status = ProcessorStatus.SUCCESS;
        String[] list = command.split(" ");
        if(list.length != 3)
            status = ProcessorStatus.INVALID_COMMAND;

        return status;
    }

    public String getOnlineUsername(String command){
        String[] list = command.split(" ");
        return list[1];
    }

    public String getFileName(String command){
        String[] list = command.split(" ");
        return list[2];
    }
}
