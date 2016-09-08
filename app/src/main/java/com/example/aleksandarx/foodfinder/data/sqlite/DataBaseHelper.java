package com.example.aleksandarx.foodfinder.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aleksandarx on 7/2/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private static String DATABASE_NAME_STRING = "FOOD_STORAGE";
    public static final String DATABASE_NAME = DataBaseHelper.DATABASE_NAME_STRING + ".db";

    private FoodTable foodTable;


    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        foodTable = new FoodTable();
        db.execSQL(foodTable.sqlCreateEntrise());
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(foodTable.sqlDeleteEntrise());
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
