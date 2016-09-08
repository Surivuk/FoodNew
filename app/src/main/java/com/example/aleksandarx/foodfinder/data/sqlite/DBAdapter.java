package com.example.aleksandarx.foodfinder.data.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.data.model.FoodModel;

import java.util.List;

/**
 * Created by aleksandarx on 7/2/16.
 */
public class DBAdapter {

    private static DBAdapter singletonAdapter = null;
    private SQLiteDatabase db = null;
    private DataBaseHelper dbHelper = null;

    private FoodTable table;

    private DBAdapter(Context context){
        dbHelper = new DataBaseHelper(context);
        table = new FoodTable();
    }

    public static DBAdapter createAdapter(Context context){
        if(singletonAdapter == null)
            singletonAdapter = new DBAdapter(context);
        return singletonAdapter;
    }

    public long insert(String tableName, FoodModel value) {
        long id = -1;

        if(db != null && db.isOpen()){
            db.beginTransaction();
            try{
                id = db.insert(tableName, null, table.insertValue(value));
                db.setTransactionSuccessful();
            }
            catch (Exception e){
                Toast.makeText(null, "INSERT Fail.", Toast.LENGTH_SHORT).show();
            }
            finally {
                db.endTransaction();
            }
            return id;
        }
        else{
            Toast.makeText(null, "INSERT Fail.", Toast.LENGTH_SHORT).show();
        }

        return id;
    }

    public int update(String tableName, FoodModel value, long id) {
        int count = 0;

        if (db != null && db.isOpen()) {
            db.beginTransaction();
            try {
                count = db.update(tableName, table.updateValue(value), table.updateClaus(id), null);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Toast.makeText(null, "UPDATE Fail.", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
            }
            return count;
        } else {
            Toast.makeText(null, "UPDATE Fail.", Toast.LENGTH_SHORT).show();
        }

        return count;
    }

    private Cursor cursor(){
        Cursor ret = null;
        if(db != null && db.isOpen()){
            db.beginTransaction();
            try{
                ret = db.query(FoodTable.getTableName(), null, null, null, null, null, null);
                db.setTransactionSuccessful();
            }
            catch (Exception e){
                Toast.makeText(null, "READ Fail.", Toast.LENGTH_SHORT).show();
            }
            finally {
                db.endTransaction();
            }
            return ret;
        }
        else{
            Toast.makeText(null, "READ Fail.", Toast.LENGTH_SHORT).show();
            return ret;
        }
    }

    public Object read(long id) {
        return table.readOne(cursor(), id);
    }

    public List<Object> readAll() {
        return table.readAll(cursor());
    }

    public void deleteAllFromTable(String tableName) {
        db.execSQL(table.deleteAll());
    }

    public boolean delete(String tableName, long id){
        boolean isDeleted = false;

        if (db != null && db.isOpen()) {
            db.beginTransaction();
            try {
                isDeleted = db.delete(tableName, table.deleteValue(id), null) > 0;
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Toast.makeText(null, "DELETE Fail.", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
            }
        } else {
            Toast.makeText(null, "DELETE Fail.", Toast.LENGTH_SHORT).show();
        }

        return isDeleted;
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if(db != null)
            db.close();
    }

}
