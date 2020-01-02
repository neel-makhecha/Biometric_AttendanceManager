package com.neelmakhecha.fingerprintauthentication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Users_DB";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE fingerprints (user_id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT,profile_photo BLOB,fingerprint_binary BLOB)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS fingerprints");
        onCreate(sqLiteDatabase);
    }

    public boolean addNewFingerprint(String username,byte[] profilePhoto,byte[] fingerprintISO){

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("username",username);
        contentValues.put("fingerprint_binary",fingerprintISO);
        contentValues.put("profile_photo",profilePhoto);

        long result = database.insert("fingerprints",null,contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getAllFingerprintsWithUsernames(){

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM fingerprints",null);
        return cursor;

    }
}
