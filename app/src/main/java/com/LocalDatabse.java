package com;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabse {
    myDbHelper myhelper;
    SQLiteDatabase dbb;

    public LocalDatabse(Context context) {
        myhelper = new myDbHelper(context);
        dbb = myhelper.getWritableDatabase();
    }

    public long insertData(String DocName, String DocId) {
        //    SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.DOC, DocName);
        contentValues.put(myDbHelper.DOCID, DocId);
        long id = dbb.insert(myDbHelper.TABLE_NAME, null, contentValues);
        dbb.close();
        return id;
    }

    public String getDriveData() {
        //  SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] columns = {myDbHelper.SNO, myDbHelper.DOC, myDbHelper.DOCID};
        Cursor cursor = dbb.query(myDbHelper.TABLE_NAME, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int cid = cursor.getInt(cursor.getColumnIndex(myDbHelper.SNO));
            String DocName = cursor.getString(cursor.getColumnIndex(myDbHelper.DOC));
            String DocId = cursor.getString(cursor.getColumnIndex(myDbHelper.DOCID));
            //   Global.storedSettings.put(type, status);
            buffer.append(cid + "   " + DocName + "   " + DocId + " \n");
        }
        cursor.close();
        dbb.close();
        return buffer.toString();
    }

    public int delete(String uname) {
        // SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs = {uname};
        int count = dbb.delete(myDbHelper.TABLE_NAME, myDbHelper.DOC + " = ?", whereArgs);
        dbb.close();
        return count;
    }


    static class myDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "camscan_india";    // Database Name
        private static final String TABLE_NAME = "SynDriveRecord";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String SNO = "_id";     // Column I (Primary Key)
        private static final String DOC = "Doc";    //Column II
        private static final String DOCID = "DocId";    // Column III
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
                " (" + SNO + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DOC + " VARCHAR(255) ," + DOCID + " VARCHAR(225));";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private Context context;

        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context = context;
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) {
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
            } catch (Exception e) {
            }
        }
    }
}

