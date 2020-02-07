package com.micool.minet.DataClasses;

public class MetaData {
    private String direction;
    private String stepId;
    private String room;

    public MetaData(String direction, String stepId, String room) {
        this.direction = direction;
        this.stepId = stepId;
        this.room = room;
    }

    public String toString(){
        return stepId+ "," + direction + "," + room;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
