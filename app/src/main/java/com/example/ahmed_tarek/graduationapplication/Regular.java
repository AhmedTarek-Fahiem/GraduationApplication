package com.example.ahmed_tarek.graduationapplication;

import java.util.UUID;

public class Regular {

    private UUID prescriptionUUID;
    private long timeStamp;

    public Regular(UUID prescriptionUUID, long timeStamp) {
        this.prescriptionUUID = prescriptionUUID;
        this.timeStamp = timeStamp;
    }

    public UUID getPrescriptionUUID() {
        return prescriptionUUID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
