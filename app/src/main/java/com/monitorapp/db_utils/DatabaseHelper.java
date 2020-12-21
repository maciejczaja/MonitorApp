package com.monitorapp.db_utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Stats.db";
    private static final int DATABASE_VERSION = 1;

    private static final int NUMBER_OF_TABLES = 13;

    /* Table names */
    private static final String TABLE_MOTION_SENSORS = "Motion_sensors";
    private static final String TABLE_MOTION_SENSOR_READINGS = "Motion_sensor_readings";
    private static final String TABLE_DATA = "Data";
    private static final String TABLE_CALL_DATA = "Call_data";
    private static final String TABLE_CALL_STATES = "Call_states";
    private static final String TABLE_SMS_DATA = "Text_message_data";
    private static final String TABLE_APP_DATA = "App_data";
    private static final String TABLE_NOISE_DETECTOR_DATA = "Noise_detector_data";
    private static final String TABLE_BATTERY_DATA = "Battery_data";
    private static final String TABLE_ON_OFF_DATA = "On_off_data";
    private static final String TABLE_NETWORK_DATA = "Network_data";
    private static final String TABLE_STATES_ON_OFF = "States_on_off";
    private static final String TABLE_TYPES_ON_OFF = "Types_on_off";

    /* Shared attribute names */
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_FK_ID_STATE = "fk_id_state";

    /* Motion sensor readings columns */
    private static final String COLUMN_X = "x_axis";
    private static final String COLUMN_Y = "y_axis";
    private static final String COLUMN_Z = "z_axis";
    private static final String COLUMN_FK_MSR = "fk_sensors_id";

    /* App data columns */
    private static final String COLUMN_PACKAGE = "package";

    /* Call data columns */
    private static final String COLUMN_FK_CD = "fk_call_state_id";
    private static final String COLUMN_DURATION = "duration";

    /* Noise detector data columns */
    private static final String COLUMN_VOLUME = "volume";
    private static final String COLUMN_DB_COUNT = "db_count";

    /* Battery data columns */
    private static final String COLUMN_BATTERY_LEVEL = "battery_level";

    /* Network data columns */
    private static final String COLUMN_NETWORK_INFO = "network_info";

    /* On off data columns */
    private static final String COLUMN_FK_ID_TYPE = "fk_id_type";

    private static DatabaseHelper instance;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String[] queries = new String [NUMBER_OF_TABLES];
        queries[0] =
                "CREATE TABLE " + TABLE_MOTION_SENSORS + " ("+
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " VARCHAR(30) NOT NULL" +
                        ");";

        queries[1] =
                "CREATE TABLE " + TABLE_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_DATETIME + " DATETIME NOT NULL," +
                        COLUMN_USER_ID + " INT NOT NULL" +
                        ");";

        queries[2] =
                "CREATE TABLE " + TABLE_MOTION_SENSOR_READINGS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES Data," +
                        COLUMN_X + " DOUBLE NOT NULL," +
                        COLUMN_Y + " DOUBLE," +
                        COLUMN_Z + " DOUBLE," +
                        COLUMN_FK_MSR + " INT NOT NULL REFERENCES Motion_sensors" +
                        ");";

        queries[3] =
                "CREATE TABLE " + TABLE_CALL_STATES + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL," +
                        COLUMN_NAME + " VARCHAR(30)" +
                        ");";

        queries[4] =
                "CREATE TABLE " + TABLE_CALL_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES Data," +
                        COLUMN_FK_CD + " INT NOT NULL REFERENCES Call_state," +
                        COLUMN_DURATION + " INT NOT NULL" +
                        ");";

        queries[5] =
                "CREATE TABLE " + TABLE_SMS_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES Data" +
                        ");";

        queries[6] =
                "CREATE TABLE " + TABLE_APP_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA," +
                        COLUMN_PACKAGE + " VARCHAR(60)" +
                        ");";

        queries[7] =
                "CREATE TABLE " + TABLE_NOISE_DETECTOR_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA," +
                        COLUMN_VOLUME + " INTEGER, " +
                        COLUMN_DB_COUNT + " FLOAT" +
                        ");";

        queries[8] =
                "CREATE TABLE " + TABLE_STATES_ON_OFF + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " VARCHAR(5)" +
                        ");";

        queries[9] =
                "CREATE TABLE " + TABLE_ON_OFF_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA, " +
                        COLUMN_FK_ID_STATE + " INT NOT NULL REFERENCES " + TABLE_STATES_ON_OFF + ", " +
                        COLUMN_FK_ID_TYPE + " INT NOT NULL REFERENCES " + TABLE_TYPES_ON_OFF +
                        ");";

        queries[10] =
                "CREATE TABLE " + TABLE_TYPES_ON_OFF + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " VARCHAR(20)" +
                        ");";

        queries[11] =
                "CREATE TABLE " + TABLE_BATTERY_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA," +
                        COLUMN_BATTERY_LEVEL + " INTEGER" +
                        ");";

        queries[12] =
                "CREATE TABLE " + TABLE_NETWORK_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA," +
                        COLUMN_NETWORK_INFO + " TEXT" +
                        ");";

        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            db.execSQL(queries[i]);
        }

        addRecordMotionSensors("Accelerometer", db);
        addRecordMotionSensors("Magnetometer", db);
        addRecordMotionSensors("Gyroscope", db);
        addRecordMotionSensors("Light", db);
        addRecordMotionSensors("Gravity", db);

        addRecordCallState("Incoming Picked up", db);
        addRecordCallState("Incoming Rejected", db);
        addRecordCallState("Incoming Missed", db);
        addRecordCallState("Outgoing", db);

        addRecordStatesOnOff("On", db);
        addRecordStatesOnOff("Off", db);

        addRecordTypesOnOff("Airplane mode", db);
        addRecordTypesOnOff("Screen", db);
        addRecordTypesOnOff("Power", db);

    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTION_SENSORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTION_SENSOR_READINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_STATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOISE_DETECTOR_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ON_OFF_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BATTERY_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORK_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TYPES_ON_OFF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATES_ON_OFF);


        onCreate(db);
    }

    void addRecordMotionSensors(String name, @NotNull SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        db.insert(TABLE_MOTION_SENSORS, null, cv);

    }

    public void addRecordMotionSensorReadings(String user_id, String datetime,
                                              Float x_axis, Float y_axis, Float z_axis,
                                              @NotNull String sensorName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvData = new ContentValues();
        ContentValues cvSensor = new ContentValues();
        String newName = "";
        int fkIdSensor = 0;
        Long lastID = Long.valueOf(0);

        if (sensorName.contains("Accelerometer"))
            newName = "Accelerometer";
        else if (sensorName.contains("Gyroscope"))
            newName = "Gyroscope";
        else if (sensorName.contains("Gravity"))
            newName = "Gravity";
        else if (sensorName.contains("Magnetometer"))
            newName = "Magnetometer";
        else if (sensorName.contains("alsprx"))
            newName = "Light";

        String[] params = new String[]{ newName };
        String[] columns = new String[] {COLUMN_ID};

        /*TODO: This Cursor should be freed up after use with #close() */
        Cursor c = db.query(TABLE_MOTION_SENSORS, columns,
                COLUMN_NAME + " = ?", params,
                null, null, null);

        if (c.moveToNext()) {
            fkIdSensor = c.getInt(0);
        }

        cvSensor.put(COLUMN_X, x_axis);
        cvSensor.put(COLUMN_Y, y_axis);
        cvSensor.put(COLUMN_Z, z_axis);
        cvSensor.put(COLUMN_FK_MSR, fkIdSensor);

        cvData.put(COLUMN_DATETIME, datetime);
        cvData.put(COLUMN_USER_ID, user_id);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvSensor.put(COLUMN_ID, lastID);
            db.insert(TABLE_MOTION_SENSOR_READINGS, null, cvSensor);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public void addRecordCallData(int callStateInt, String userID, String datetime, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvCall = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long fkIdCallState = 0L;
        Long lastID = 0L;
        String callState = "";

        if (callStateInt == 1) {
            callState = "Incoming Picked up";
        } else if (callStateInt == 3) {
            callState = "Incoming Missed";
        } else if (callStateInt == 5) {
            callState = "Incoming Rejected";
        } else {
            callState = "Outgoing";
        }

        String[] params = new String[]{ callState };
        String[] columns = new String[] {COLUMN_ID};
        Cursor c = db.query(TABLE_CALL_STATES, columns,
                COLUMN_NAME + " = ?", params,
                null, null, null);

        if (c.moveToNext()) {
            fkIdCallState = c.getLong(0);
        }

        cvData.put(COLUMN_USER_ID, userID);
        cvData.put(COLUMN_DATETIME, datetime);

        cvCall.put(COLUMN_FK_CD, fkIdCallState);
        cvCall.put(COLUMN_DURATION, duration);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvCall.put(COLUMN_ID, lastID);
            db.insert(TABLE_CALL_DATA, null, cvCall);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

    }

    void addRecordCallState(String name, @NotNull SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);

        long result = db.insert(TABLE_CALL_STATES, null, cv);
    }

    public void addRecordTextMessageData(String userID, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long lastID = 0L;

        cvData.put(COLUMN_USER_ID, userID);
        cvData.put(COLUMN_DATETIME, datetime);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cv.put(COLUMN_ID, lastID);
            db.insert(TABLE_SMS_DATA, null, cv);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

    }

    public void addRecordAppData(String packageName, String datetime, String userID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvApp = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long lastID = 0L;

        cvData.put(COLUMN_DATETIME, datetime);
        cvData.put(COLUMN_USER_ID, userID);

        cvApp.put(COLUMN_PACKAGE, packageName);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvApp.put(COLUMN_ID, lastID);
            db.insert(TABLE_APP_DATA, null, cvApp);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

    }

    public void addRecordNoiseDetectorData(String datetime, String userID, int volume, float dbCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvNoiseDetector = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long lastID = Long.valueOf(0);

        cvData.put(COLUMN_DATETIME, datetime);
        cvData.put(COLUMN_USER_ID, userID);

        cvNoiseDetector.put(COLUMN_VOLUME, volume);
        cvNoiseDetector.put(COLUMN_DB_COUNT, dbCount);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvNoiseDetector.put(COLUMN_ID, lastID);
            db.insert(TABLE_NOISE_DETECTOR_DATA, null, cvNoiseDetector);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public void addRecordTypesOnOff(String name, @NotNull SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);

        db.insert(TABLE_TYPES_ON_OFF, null, cv);
    }

    public void addRecordStatesOnOff(String name, @NotNull SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);

        db.insert(TABLE_STATES_ON_OFF, null, cv);
    }

    public void addRecordBatteryData(String datetime, String userID, float batteryLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvBattery = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long lastID = Long.valueOf(0);

        cvData.put(COLUMN_DATETIME, datetime);
        cvData.put(COLUMN_USER_ID, userID);

        cvBattery.put(COLUMN_BATTERY_LEVEL, batteryLevel);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvBattery.put(COLUMN_ID, lastID);
            db.insert(TABLE_BATTERY_DATA, null, cvBattery);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public void addRecordOnOffData(String type, String state, String userID, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvCall = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long fkIdOnOffState = Long.valueOf(0);
        Long fkIdOnOffType = Long.valueOf(0);
        Long lastID = Long.valueOf(0);

        String[] params = new String[]{ type };
        String[] columns = new String[] {COLUMN_ID};
        Cursor c = db.query(TABLE_TYPES_ON_OFF, columns,
                COLUMN_NAME + " = ?", params,
                null, null, null);

        if (c.moveToNext()) {
            fkIdOnOffType = c.getLong(0);
        }

        String[] params2 = new String[]{ state };
        String[] columns2 = new String[] {COLUMN_ID};
        Cursor c2 = db.query(TABLE_STATES_ON_OFF, columns2,
                COLUMN_NAME + " = ?", params2,
                null, null, null);

        if (c2.moveToNext()) {
            fkIdOnOffState = c2.getLong(0);
        }

        cvData.put(COLUMN_USER_ID, userID);
        cvData.put(COLUMN_DATETIME, datetime);

        cvCall.put(COLUMN_FK_ID_STATE, fkIdOnOffState);
        cvCall.put(COLUMN_FK_ID_TYPE, fkIdOnOffType);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvCall.put(COLUMN_ID, lastID);
            db.insert(TABLE_ON_OFF_DATA, null, cvCall);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

    }

    public void addRecordNetworkData(String datetime, String userID, String info) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvNetwork = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long lastID = Long.valueOf(0);

        cvData.put(COLUMN_DATETIME, datetime);
        cvData.put(COLUMN_USER_ID, userID);

        cvNetwork.put(COLUMN_NETWORK_INFO, info);

        db.beginTransaction();

        try {

            lastID = db.insert(TABLE_DATA, null, cvData);
            cvNetwork.put(COLUMN_ID, lastID);
            db.insert(TABLE_NETWORK_DATA, null, cvNetwork);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    @NotNull
    public static List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        tables.add(TABLE_APP_DATA);
        tables.add(TABLE_CALL_DATA);
        tables.add(TABLE_CALL_STATES);
        tables.add(TABLE_DATA);
        tables.add(TABLE_MOTION_SENSOR_READINGS);
        tables.add(TABLE_MOTION_SENSORS);
        tables.add(TABLE_SMS_DATA);
        tables.add(TABLE_NOISE_DETECTOR_DATA);
        tables.add(TABLE_NETWORK_DATA);
        tables.add(TABLE_BATTERY_DATA);
        tables.add(TABLE_ON_OFF_DATA);
        tables.add(TABLE_STATES_ON_OFF);
        tables.add(TABLE_TYPES_ON_OFF);

        return tables;
    }
}