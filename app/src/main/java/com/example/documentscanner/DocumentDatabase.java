package com.example.documentscanner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {Document.class}, version = 2, exportSchema = false)
public abstract class DocumentDatabase extends RoomDatabase {

    private static DocumentDatabase INSTANCE;

    public abstract DocumentDao documentDao();

    private static final Object sLock = new Object();

    public static DocumentDatabase getInstance( Context context ) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DocumentDatabase.class,
                        "documents.db"
                )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return INSTANCE;
        }
    }


}
