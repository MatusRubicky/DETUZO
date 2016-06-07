package com.example.matusrubicky.detuzo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DETUZODatabaseOpenHelper extends SQLiteOpenHelper {

    public DETUZODatabaseOpenHelper(Context context) {
        super(context, "DETUZO", null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE %s (" +
                "%s INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "%s NAME," + "%s TIME," +"%s SPEED," + "%s ELEVATION," + "%s PATH" +
                ")";

        db.execSQL(String.format(sql,
                DETUZO.route.TABLE_NAME,
                DETUZO.route._ID,
                DETUZO.route.NAME,
                DETUZO.route.TIME,
                DETUZO.route.SPEED,
                DETUZO.route.ELEVATION,
                DETUZO.route.PATH
        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DETUZO.route.TABLE_NAME);
        onCreate(db);
    }

    public List<String> getAllPaths() {
        List<String> list = new ArrayList<>();
        String selectQuery = "SELECT "+DETUZO.route.PATH+" FROM " + DETUZO.route.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(0);
                list.add(path);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.reverse(list);
        return list;
    }
}