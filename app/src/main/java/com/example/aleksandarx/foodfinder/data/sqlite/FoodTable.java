package com.example.aleksandarx.foodfinder.data.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.aleksandarx.foodfinder.data.model.FoodModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleksandarx on 7/2/16.
 */
public class FoodTable {

    private static final String TABLE_NAME = "FOOD_ARTICLE";
    private static final String ID = "ARTICLE_ID";
    private static final String DB_ID = "DB_ID";
    private static final String ARTICLE_USER = "user_id";
    private static final String ARTICLE_LOCATION = "locationAddress";
    private static final String ARTICLE_LOCATION_ID = "place_id";
    private static final String ARTICLE_LOCATION_NAME = "locationName";
    private static final String ARTICLE_NAME = "articleName";
    private static final String ARTICLE_DESCRIPTION = "articleDescription";
    private static final String ARTICLE_FOOD_TYPE = "foodType";
    private static final String MEAL_TYPE = "mealType";
    private static final String ARTICLE_ORIGIN = "origin";
    private static final String ARTICLE_IMAGE = "ARTICLE_IMAGE";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FoodTable.TABLE_NAME + " (" +
                    FoodTable.ID + " INTEGER PRIMARY KEY," +
                    FoodTable.DB_ID + " INTEGER," +
                    FoodTable.ARTICLE_USER + " INTEGER," +
                    FoodTable.ARTICLE_LOCATION_ID + " TEXT," +
                    FoodTable.ARTICLE_LOCATION + " TEXT," +
                    FoodTable.ARTICLE_LOCATION_NAME + " TEXT," +
                    FoodTable.ARTICLE_NAME + " TEXT," +
                    FoodTable.ARTICLE_DESCRIPTION + " TEXT," +
                    FoodTable.MEAL_TYPE + " TEXT," +
                    FoodTable.ARTICLE_FOOD_TYPE + " TEXT," +
                    FoodTable.ARTICLE_ORIGIN + " TEXT," +
                    FoodTable.ARTICLE_IMAGE + " TEXT" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FoodTable.TABLE_NAME;

    public FoodTable(){}

    public static String getTableName() { return TABLE_NAME; }

    public String sqlCreateEntrise(){ return SQL_CREATE_ENTRIES; }

    public String sqlDeleteEntrise(){ return SQL_DELETE_ENTRIES; }

    public ContentValues insertValue(FoodModel input){
        ContentValues val = new ContentValues();
        String[] tmp = FoodModel.FIELDS;
        for(int i = 0; i < tmp.length; i ++){
            String tmpValue = input.getItem(tmp[i]);
            if(!tmpValue.equals("ERROR")){
                val.put(tmp[i], tmpValue);
            }
        }
        if(input.getArticle_image() != null)
            val.put(ARTICLE_IMAGE, input.getArticle_image());
        val.put(DB_ID, input.getDb_id());
        return val;
    }

    public String deleteValue(long id) {
        return this.ID + "=" + id;
    }

    public ContentValues updateValue(FoodModel value) {
        ContentValues val = new ContentValues();
        return val;
    }

    public String updateClaus(long id) {
        return this.ID + "=" + id;
    }

    public Object readOne(Cursor cursor, long id) {
        Object ret = null;
        if(cursor != null){
            while (cursor.moveToNext()){
                long id_tmp = cursor.getLong(cursor.getColumnIndex(ID));
                if(id == id_tmp){
                    FoodModel model = createModel(cursor);
                    model.setArticle_id(id);
                    ret = model;
                    break;
                }
            }
        }
        return ret;
    }

    public List<Object> readAll(Cursor cursor) {
        List<Object> ret = new ArrayList<>();
        if(cursor != null){
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(ID));
                FoodModel model = createModel(cursor);
                model.setArticle_id(id);
                ret.add(model);
            }
        }
        return ret;
    }

    public String deleteAll(){
        return "DELETE FROM " + getTableName();
    }

    private FoodModel createModel(Cursor cursor){
        List<String> vals = new ArrayList<>();
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_USER)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_NAME)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_DESCRIPTION)));
        vals.add("xxxxxxxx"); // isFood
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_ORIGIN)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_FOOD_TYPE)));
        vals.add(cursor.getString(cursor.getColumnIndex(MEAL_TYPE)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_LOCATION_NAME)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_LOCATION)));
        vals.add(cursor.getString(cursor.getColumnIndex(ARTICLE_LOCATION_ID)));
        FoodModel model = new FoodModel();
        String[] tmp = FoodModel.FIELDS;
        for(int i = 0; i < tmp.length; i++){
            if(tmp[i].equals("isFood"))
                continue;
            if(tmp[i].equals("locationLat"))
                break;
            model.addItem(tmp[i], vals.get(i));
        }
        model.setArticle_image(cursor.getString(cursor.getColumnIndex(ARTICLE_IMAGE)));
        model.setDb_id(Long.parseLong(cursor.getString(cursor.getColumnIndex(DB_ID))));
        return model;
    }

}
