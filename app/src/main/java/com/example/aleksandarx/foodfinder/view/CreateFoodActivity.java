package com.example.aleksandarx.foodfinder.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.network.HttpHelper;
import com.example.aleksandarx.foodfinder.share.UserPreferences;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CreateFoodActivity extends AppCompatActivity {

    private LinearLayout foodPanel;
    private byte[] imgBuffer;
    private String imgUri;
    private JSONObject restaurant;

    private Bitmap tmpBitmap;


    private EditText foodName;
    private EditText foodDescription;
    private Spinner foodType;
    private Spinner mealType;
    private Spinner foodOrigin;
    private Switch foodOrDrink;

    private Handler guiThread;

    private Button foodImageButton;
    private Button mapButton;

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_food);

        guiThread = new Handler();
        imgBuffer = null;
        tmpBitmap = null;

        foodType = (Spinner) findViewById(R.id.food_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.food_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodType.setAdapter(adapter);

        mealType = (Spinner) findViewById(R.id.meal_type_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.meal_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealType.setAdapter(adapter);

        foodOrigin = (Spinner) findViewById(R.id.origin_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.origin_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodOrigin.setAdapter(adapter);

        foodName = (EditText) findViewById(R.id.food_name);
        foodDescription = (EditText) findViewById(R.id.description_text_box);

        foodOrDrink = (Switch) findViewById(R.id.food_drink_switch);
        foodOrDrink.setChecked(true);

        foodPanel = (LinearLayout) findViewById(R.id.food_panel);

        foodOrDrink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    buttonView.setText("Food");
                    foodPanel.setVisibility(View.VISIBLE);
                }
                else {
                    buttonView.setText("Drink");
                    foodPanel.setVisibility(View.INVISIBLE);
                }
            }
        });

        foodImageButton = (Button) findViewById(R.id.food_image_button);

        foodImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(CreateFoodActivity.this, CameraActivity.class), 16);
            }
        });

        mapButton = (Button) findViewById(R.id.find_at_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(CreateFoodActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        Button submitFood = (Button) findViewById(R.id.submit_food);
        if(submitFood != null) {
            submitFood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (restaurant != null && !foodName.getEditableText().toString().isEmpty()) {
                        ExecutorService t = Executors.newSingleThreadExecutor();
                        t.submit(new Runnable() {
                            @Override
                            public void run() {
                                if (imgBuffer == null && tmpBitmap != null) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    tmpBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                                    imgBuffer = stream.toByteArray();
                                    tmpBitmap = null;
                                }
                                guiNotifyUser(HttpHelper.newFood(imgBuffer, createFoodObject()));
                            }
                        });
                    } else {
                        Toast.makeText(CreateFoodActivity.this, "No name of place!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String unicodeEscape(String input){
        StringBuilder b = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c >= 128)
                b.append("\\u").append(String.format("%04X", (int) c));
            else
                b.append(c);
        }

        return b.toString();
    }
    private String decodeUnicodeEscape(String input)
    {
        try{
            char[] array = input.toCharArray();
            String text = "";
            for(int i = 0 ; i < array.length; i++)
            {
                if(array[i] == '\\')
                {
                    String uniBuff = "";
                    uniBuff += array[i+2];
                    uniBuff += array[i+3];
                    uniBuff += array[i+4];
                    uniBuff += array[i+5];

                    int hexVal = Integer.parseInt(uniBuff,16);

                    text += (char) hexVal;

                    i = i + 5;

                }
                else{
                    text += array[i];
                }
            }
            return text;
        }
        catch(Exception ex)
        {
            return "Error";
        }



    }

    private HashMap<String, String> createFoodObject(){
        HashMap<String, String> obj = new HashMap<>();
        obj.put("user_id", UserPreferences.getPreference(CreateFoodActivity.this, UserPreferences.USER_ID));
        if(!foodName.getText().toString().isEmpty())
            obj.put("articleName", foodName.getText().toString());

        if(!foodDescription.getText().toString().isEmpty())
            obj.put("articleDescription", foodDescription.getText().toString());

        if(foodOrDrink.isChecked()){
            obj.put("isFood", "on");
            obj.put("origin", foodOrigin.getSelectedItem().toString());
            obj.put("foodType", foodType.getSelectedItem().toString());
            obj.put("mealType", mealType.getSelectedItem().toString());
        }
        else
            obj.put("isFood", "off");

        try {
            obj.put("locationName", unicodeEscape(restaurant.getString("name")));
            obj.put("locationAddress", unicodeEscape(restaurant.getString("vicinity")));
            obj.put("place_id", restaurant.getString("place_id"));
            obj.put("locationLat", restaurant.getString("lat"));
            obj.put("locationLng", restaurant.getString("lng"));


            String deco1 = decodeUnicodeEscape(unicodeEscape(restaurant.getString("name")));
            String deco2 = decodeUnicodeEscape(unicodeEscape(restaurant.getString("vicinity")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }


    private void guiNotifyUser(final String message)
    {
        guiThread.post(new Runnable(){
            public void run(){
                Toast.makeText(CreateFoodActivity.this, message, Toast.LENGTH_LONG).show();
                startActivity(new Intent(CreateFoodActivity.this, FoodArticlesActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if(resultCode == Activity.RESULT_OK && requestCode == 16){
                String result = data.getStringExtra("uri");
                Uri imageUri = Uri.parse(result);
                imgUri = result;
                try {
                    tmpBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    foodImageButton.setBackgroundColor(Color.rgb(0,150,136));
                    foodImageButton.setTextColor(Color.WHITE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
                final Place place = PlacePicker.getPlace(this, data);
                if (place.getPlaceTypes().contains(Place.TYPE_FOOD)) {
                    restaurant = new JSONObject();
                    try {
                        restaurant.put("name", place.getName());
                        restaurant.put("vicinity", place.getAddress());
                        restaurant.put("place_id", place.getId());
                        restaurant.put("lat", place.getLatLng().latitude);
                        restaurant.put("lng", place.getLatLng().longitude);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
    }
}
