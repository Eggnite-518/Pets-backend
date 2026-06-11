package com.example.pets_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pets.fulfillment-protection")
public class FulfillmentProtectionProperties {

    private int arrivalTimeoutMinutes = 30;
    private int inactivityTimeoutMinutes = 30;
    private int ownerResponseTimeoutMinutes = 30;
    private String scanCron = "0 */5 * * * ?";

    public int getArrivalTimeoutMinutes() {
        return arrivalTimeoutMinutes;
    }

    public void setArrivalTimeoutMinutes(int arrivalTimeoutMinutes) {
        this.arrivalTimeoutMinutes = arrivalTimeoutMinutes;
    }

    public int getInactivityTimeoutMinutes() {
        return inactivityTimeoutMinutes;
    }

    public void setInactivityTimeoutMinutes(int inactivityTimeoutMinutes) {
        this.inactivityTimeoutMinutes = inactivityTimeoutMinutes;
    }

    public int getOwnerResponseTimeoutMinutes() {
        return ownerResponseTimeoutMinutes;
    }

    public void setOwnerResponseTimeoutMinutes(int ownerResponseTimeoutMinutes) {
        this.ownerResponseTimeoutMinutes = ownerResponseTimeoutMinutes;
    }

    public String getScanCron() {
        return scanCron;
    }

    public void setScanCron(String scanCron) {
        this.scanCron = scanCron;
    }
}

