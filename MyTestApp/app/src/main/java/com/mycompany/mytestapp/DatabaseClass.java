package com.mycompany.mytestapp;

import android.content.Context;
import android.database.sqlite.*;
import android.provider.BaseColumns;

/**
 * Created by Tomek on 2015-08-02.
 */
public class DatabaseClass extends SQLiteOpenHelper{

    public static abstract class PostsTable implements BaseColumns {
        public static final String TABLE_NAME = "Posts";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_USER_ID = "userid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BODY = "body";
    }

    public static abstract class ToSendTable implements BaseColumns {
        public static final String TABLE_NAME = "Tosend";
        public static final String COLUMN_NAME_USER_ID = "userid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BODY = "body";
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DownloadedPosts.db";
    public static final String TEXT_TYPE = " TEXT";

    private static final String SQL_CREATE_POSTS =
            "CREATE TABLE IF NOT EXISTS " + PostsTable.TABLE_NAME +
            " ( " +
            PostsTable._ID + " INTEGER PRIMARY KEY," +
            PostsTable.COLUMN_NAME_ID + TEXT_TYPE + "," +
            PostsTable.COLUMN_NAME_USER_ID + TEXT_TYPE + "," +
            PostsTable.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
            PostsTable.COLUMN_NAME_BODY + TEXT_TYPE +
            " ) ";

    private static final String SQL_CREATE_TOSEND =
            "CREATE TABLE IF NOT EXISTS " + ToSendTable.TABLE_NAME +
            " ( " +
            ToSendTable._ID + " INTEGER PRIMARY KEY," +
            ToSendTable.COLUMN_NAME_USER_ID + TEXT_TYPE + "," +
            ToSendTable.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
            ToSendTable.COLUMN_NAME_BODY + TEXT_TYPE +
            " ) ";


    private static final String SQL_DELETE_POSTS =
            "DROP TABLE IF EXISTS " + PostsTable.TABLE_NAME;

    private static final String SQL_DELETE_TOSEND =
            "DROP TABLE IF EXISTS " + ToSendTable.TABLE_NAME;


    public DatabaseClass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_POSTS);
        db.execSQL(SQL_CREATE_TOSEND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_POSTS);
        db.execSQL(SQL_DELETE_TOSEND);
        onCreate(db);
    }
}
