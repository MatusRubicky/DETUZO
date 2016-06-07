package com.example.matusrubicky.detuzo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DETUZOContentProvider extends ContentProvider {

    private DETUZODatabaseOpenHelper dbHelper;

    public DETUZOContentProvider() {
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new DETUZODatabaseOpenHelper(getContext());

        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DETUZO.route.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(DETUZO.route.CONTENT_URI, null);
        return ContentUris.withAppendedId(DETUZO.route.CONTENT_URI, id);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DETUZO.route.TABLE_NAME, null, null, null, null, null, "_ID DESC");
        cursor.setNotificationUri(getContext().getContentResolver(), DETUZO.route.CONTENT_URI);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
