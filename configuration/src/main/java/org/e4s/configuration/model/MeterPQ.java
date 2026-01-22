package org.e4s.configuration.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MeterPQ implements Serializable {

    private UUID key;

    private float voltage;

    private float current;

    public MeterPQ() {
    }

    public MeterPQ(UUID key, float voltage, float current) {
        this.key = key;
        this.voltage = voltage;
        this.current = current;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MeterPQ meterPQ)) {
            return false;
        }
        return Float.compare(getVoltage(), meterPQ.getVoltage()) == 0
            && Float.compare(getCurrent(), meterPQ.getCurrent()) == 0 && Objects.equals(getKey(),
                                                                                        meterPQ.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getVoltage(), getCurrent());
    }

}
