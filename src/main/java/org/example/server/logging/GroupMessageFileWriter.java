package org.example.server.logging;

public class GroupMessageFileWriter extends CustomFileWriter{

    private static final String FILE_NAME = "messagelog";
    private static boolean isFileCreated = false;
    public GroupMessageFileWriter(String prefixGroupName){
        if(!isFileCreated){
            isFileCreated = this.createFile(prefixGroupName+"_"+FILE_NAME,FILE_EXTENSION);
        }
    }
}
