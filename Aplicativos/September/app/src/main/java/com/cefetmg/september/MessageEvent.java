package com.cefetmg.september;

public class MessageEvent {

    private String console;
    private int status;
    private int waitingTime;
    private int receivedIntsAmount;
    private int sentIntsAmount;
    private boolean shouldSpin;

    public boolean isShouldSpin() {
        return shouldSpin;
    }

    public void setShouldSpin(boolean shouldSpin) {
        this.shouldSpin = shouldSpin;
    }

    public String getConsole() {
        return console;
    }

    public void setConsole(String console) {
        this.console = console;
    }

    //Status:
    //0: Stopped
    //1: Started Connection (FirstConnect)
    //2: Superconnection (waiting)
    //3: Superconnection (Connected)
    //4: Sending to server

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getReceivedIntsAmount() {
        return receivedIntsAmount;
    }

    public void setReceivedIntsAmount(int receivedIntsAmount) {
        this.receivedIntsAmount = receivedIntsAmount;
    }

    public int getSentIntsAmount() {
        return sentIntsAmount;
    }

    public void setSentIntsAmount(int sentIntsAmount) {
        this.sentIntsAmount = sentIntsAmount;
    }




}
