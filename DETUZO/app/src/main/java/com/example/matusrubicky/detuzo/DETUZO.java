package com.example.matusrubicky.detuzo;

import android.net.Uri;
import android.provider.BaseColumns;

public abstract class DETUZO {
    public static class route implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://sk.upjs.ics.android.provider/detuzo");
        public static String TABLE_NAME = "detuzo";
        public static String PATH = "path";
        public static String NAME = "name";
        public static String TIME = "time";
        public static String SPEED = "speed";
        public static String ELEVATION = "elevation";
        public static String PACKAGE = "com.example.matusrubicky.detuzo";
    }


}