package org.example.server.commandprocessor;

import java.util.ArrayList;

public class Group {
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    private String owner;
    private final ArrayList<String> invitedMembers;

    public ArrayList<String> getJoinedMembers() {
        return joinedMembers;
    }

    private final ArrayList<String> joinedMembers;

    public Group(){
        this.invitedMembers = new ArrayList<>();
        this.joinedMembers = new ArrayList<>();
    }

    public void addInvitedMember(String username){
        this.invitedMembers.add(username);
    }

    public boolean hasUserBeenInvited(String username){
        return this.invitedMembers.contains(username);
    }

    public boolean hasUserJoined(String username){
        return this.joinedMembers.contains(username);
    }

    public void addJoinedMember(String username){
        this.joinedMembers.add(username);
    }

    public ArrayList<String> getInvitedMembers() {
        return this.invitedMembers;
    }

}
