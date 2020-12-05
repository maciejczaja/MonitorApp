package com.monitorapp.enums;

public enum SensorType {
    TYPE_ACCELEROMETER(1),
    TYPE_MAGNETIC_FIELD(2),
    TYPE_GYROSCOPE(4),
    TYPE_LIGHT(5),
    TYPE_GRAVITY(9);

    private int value;

    SensorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
