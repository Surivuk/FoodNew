package com.example.aleksandarx.foodfinder.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.data.model.FoodModel;
import com.example.aleksandarx.foodfinder.data.sqlite.DBAdapter;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.example.aleksandarx.foodfinder.share.UserPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodArticlesActivity extends AppCompatActivity {

    private List<FoodModel> listValues;
    //private TextView text;
    private ListView listV;
    private Handler guiThread;
    private DBAdapter db;
    private TextView defaultText;
    private boolean isFriend = false;
    private int id;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_articles);
        guiThread = new Handler();
        listV = (ListView) findViewById(R.id.myList);
        defaultText = (TextView) findViewById(R.id.empty_list_placeholder);
        progressBar = (ProgressBar) findViewById(R.id.articles_list_progress);

        if(getIntent().hasExtra("friend")) {
            isFriend = true;
            id = getIntent().getExtras().getInt("friend");
        }
        else {
            id = Integer.parseInt(UserPreferences.getPreference(FoodArticlesActivity.this, UserPreferences.USER_ID));
            getOverflowMenu();
        }

        listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(FoodArticlesActivity.this, FoodViewActivity.class);
                FoodModel model = (FoodModel) parent.getAdapter().getItem(position);
                i.putExtra("id", model.getArticle_id());
                if(isFriend)
                    i.putExtra("friend", true);
                startActivity(i);
            }
        });

        new NetworkThread().execute();
        listV.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        defaultText.setVisibility(View.INVISIBLE);
    }

    private class NetworkThread extends AsyncTask<Void, Void, Void> {

        boolean isOk = true;

        @Override
        protected void onPreExecute() {
            listValues = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                JSONArray objects = UserNetworkController.getArticles(Connections.liveServerURL + "articles?id=" + id);
                for(int i = 0; i < objects.length(); i++){
                    JSONObject tmp = objects.getJSONObject(i);
                    FoodModel model = new FoodModel();
                    model.setArticle_id(tmp.getLong("article_id"));
                    model.setArticle_location_id(tmp.getLong("article_user"));
                    model.setArticle_name(tmp.getString("article_name"));
                    model.setArticle_origin(tmp.getString("article_origin"));
                    listValues.add(model);
                }
            } catch (Exception e){
                e.printStackTrace();
                isOk = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isOk){
                if(listValues != null){
                    progressBar.setVisibility(View.INVISIBLE);
                    listV.setVisibility(View.VISIBLE);
                    defaultText.setVisibility(View.VISIBLE);
                    ArrayAdapter<FoodModel> myAdapter = new ArrayAdapter <>(FoodArticlesActivity.this, R.layout.row_layout, R.id.listText, listValues);
                    listV.setAdapter(myAdapter);
                    if(listV.getAdapter().getCount() > 0)
                        defaultText.setVisibility(View.INVISIBLE);
                }
            }
            else {
                Toast.makeText(FoodArticlesActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isFriend) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.food_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_food:
                Intent intent = new Intent(FoodArticlesActivity.this, CreateFoodActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
