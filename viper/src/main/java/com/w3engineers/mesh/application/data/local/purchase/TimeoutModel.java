package com.w3engineers.mesh.application.data.local.purchase;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

public class TimeoutModel {

    private int timeoutPointer;
    private String receiverId;
    private int purpose;
    private int counter;

    public int getTimeoutPointer() {
        return timeoutPointer;
    }

    public TimeoutModel setTimeoutPointer(int timeoutPointer) {
        this.timeoutPointer = timeoutPointer;
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public TimeoutModel setReceiverId(String receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public int getPurpose() {
        return purpose;
    }

    public TimeoutModel setPurpose(int purpose) {
        this.purpose = purpose;
        return this;
    }

    public int getCounter() {
        return counter;
    }

    public TimeoutModel setCounter(int counter) {
        this.counter = counter;
        return this;
    }
}
