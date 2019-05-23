package com.example.labwork4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context){
        super(context, "DateDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Date(" +
                " id            INTEGER NOT NULL PRIMARY KEY, " +
                " DayOfMonth    INTEGER NOT NULL, " +
                " Month         INTEGER NOT NULL," +
                " Year          INTEGER NOT NULL, " +
                " Started       INTEGER NOT NULL" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void setDate( int dayOfMonth, int month, int year, int started, int widgetID){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put ("DayOfMonth", dayOfMonth);
        cv.put ("Month", month);
        cv.put ("Year", year);
        cv.put ("Started", started);
        Cursor c = db.rawQuery("SELECT * FROM Date WHERE id = '" + widgetID + "'", null);
        if (c.moveToFirst())
            db.update("Date", cv, "id = ?", new String[]{Integer.toString(widgetID)});
        else {
            cv.put("id", widgetID);
            db.insert("Date", null, cv);
        }
        c.close();
    }

    public Integer[] getDate(int widgetID){
        Integer[] date = new Integer[4];
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Date WHERE id = '" + widgetID + "'", null);
        if (c.moveToFirst()){
            date[0] = c.getInt(1);
            date[1] = c.getInt(2);
            date[2] = c.getInt(3);
            date[3] = c.getInt(4);
            c.close();
        }else
            return null;
        return date;
    }

    public void setStarted(int started, int widgetID){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put ("Started", started);
        db.update("Date", cv, "id = ?", new String[]{Integer.toString(widgetID)});
    }

    public void deleteDate(int widgetID){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("Date", "id = ?", new String[]{Integer.toString(widgetID)});
    }
}
