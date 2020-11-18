package com.monitorapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "Stats.db";
    private static final int DATABASE_VERSION = 1;

    private static final int NUMBER_OF_TABLES = 7;

    //Table names
    private static final String TABLE_MOTION_SENSORS = "Motion_sensors";
    private static final String TABLE_MOTION_SENSOR_READINGS = "Motion_sensor_readings";
    private static final String TABLE_DATA = "Data";
    private static final String TABLE_CALL_DATA = "Call_data";
    private static final String TABLE_CALL_STATES = "Call_states";
    private static final String TABLE_SMS_DATA = "Text_message_data";
    private static final String TABLE_APP_DATA = "App_data";

    //Shared attribute names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DATETIME = "datetime";

    //Motion sensor readings columns
    private static final String COLUMN_X = "x_axis";
    private static final String COLUMN_Y = "y_axis";
    private static final String COLUMN_Z = "z_axis";
    private static final String COLUMN_FK_MSR = "fk_sensors_id";

    //App data columns
    private static final String COLUMN_PACKAGE = "package";
    private static final String COLUMN_PROCESS = "process";
    private static final String COLUMN_FK_AD = "fk_event_types_id";

    //Call data columns
    private static final String COLUMN_FK_CD = "fk_call_state_id";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID + " TEXT, " +
                        COLUMN_PACKAGE + " TEXT, " +
                        COLUMN_PROCESS + " TEXT, " +
                        COLUMN_TIME + " TEXT);";*/
        String queries[] = new String [NUMBER_OF_TABLES];
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
                        COLUMN_FK_CD + " INT NOT NULL REFERENCES Call_state" +
                        ");";

        queries[5] =
                "CREATE TABLE " + TABLE_SMS_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES Data" +
                        ");";

        queries[6] =
                "CREATE TABLE " + TABLE_APP_DATA + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL REFERENCES DATA," +
                        COLUMN_PACKAGE + " VARCHAR(60)," +
                        COLUMN_PROCESS + " VARCHAR(60)" +
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTION_SENSORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTION_SENSOR_READINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_STATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_DATA);

        onCreate(db);
    }

    void addRecordMotionSensors(String name, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        db.insert(TABLE_MOTION_SENSORS, null, cv);

    }

    void addRecordMotionSensorReadings(String user_id, String datetime,
                                       Float x_axis, Float y_axis, Float z_axis,
                                       String sensorName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvData = new ContentValues();
        ContentValues cvSensor = new ContentValues();
        String newName = "";
        int fkIdSensor = 0;

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

        db.beginTransactionNonExclusive();

        try {

            db.insert(TABLE_DATA, null, cvData);
            db.insert(TABLE_MOTION_SENSOR_READINGS, null, cvSensor);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    void addRecordData(String datetime, Long user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, user_id);
        cv.put(COLUMN_DATETIME, datetime);

        long result = db.insert(TABLE_DATA, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Adding to database failed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Successfully added to database", Toast.LENGTH_SHORT).show();
        }

    }

    void addRecordCallData(String callState, String userID, String datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvCall = new ContentValues();
        ContentValues cvData = new ContentValues();
        Long fkIdCallState = Long.valueOf(0);

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

        db.beginTransactionNonExclusive();

        try {

            db.insert(TABLE_DATA, null, cvData);
            db.insert(TABLE_CALL_DATA, null, cvCall);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

    }

    void addRecordCallState(String name, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);

        long result = db.insert(TABLE_CALL_STATES, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Adding to database failed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Successfully added to database", Toast.LENGTH_SHORT).show();
        }

    }

    void addRecordTextMessageData() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        long result = db.insert(TABLE_SMS_DATA, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Adding to database failed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Successfully added to database", Toast.LENGTH_SHORT).show();
        }

    }

    void addRecordAppData(String packageName, String process) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PACKAGE, packageName);
        cv.put(COLUMN_PROCESS, process);

        long result = db.insert(TABLE_APP_DATA, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Adding to database failed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Successfully added to database", Toast.LENGTH_SHORT).show();
        }

    }

    SQLiteDatabase getDatabase() {
        return this.getWritableDatabase();
    }

    public static List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        tables.add(TABLE_APP_DATA);
        tables.add(TABLE_CALL_DATA);
        tables.add(TABLE_CALL_STATES);
        tables.add(TABLE_DATA);
        tables.add(TABLE_MOTION_SENSOR_READINGS);
        tables.add(TABLE_MOTION_SENSORS);
        tables.add(TABLE_SMS_DATA);

        return tables;
    }
}
