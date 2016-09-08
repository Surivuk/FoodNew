package com.example.aleksandarx.foodfinder.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.network.controller.UserNetworkController;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.internal.io.FileSystem;

public class SignUpActivity extends AppCompatActivity {

    private Uri imgUri;
    private boolean isCorrect;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button takePhoto = (Button) findViewById(R.id.sign_up_take_profile_photo_button);
        if(takePhoto != null)
            takePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(SignUpActivity.this, CameraActivity.class), 16);
                }
            });

        isCorrect = false;

        final TextView email = (TextView) findViewById(R.id.sing_up_username_edit_text);
        final TextView password = (TextView) findViewById(R.id.sing_up_password_edit_text);
        final TextView rePassword = (TextView) findViewById(R.id.sing_up_password_confirm_edit_text);
        final TextView firstName = (TextView) findViewById(R.id.sing_up_first_name_edit_text);
        final TextView lastName = (TextView) findViewById(R.id.sing_up_last_name_edit_text);
        final TextView phone = (TextView) findViewById(R.id.sing_up_phone_number_edit_text);

        Button signUp = (Button) findViewById(R.id.sign_up_button);
        if(signUp != null)
            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    data = new JSONObject();
                    try {
                        data.put("email", email.getText().toString());
                        if(password.getEditableText().toString().equals(rePassword.getEditableText().toString()))
                            data.put("password", password.getEditableText().toString());
                        data.put("firstName", firstName.getText().toString());
                        data.put("lastName", lastName.getText().toString());
                        data.put("phone", phone.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    new MyThread().execute();
                }
            });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK && requestCode == 16){
            String result = data.getStringExtra("uri");
            imgUri = Uri.parse(result);
        }
    }

    private class MyThread extends AsyncTask<Void, Void, Void>{

        boolean isOk = false;
        Uri newUri;

        @Override
        protected void onPreExecute() {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                newUri = Uri.parse(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(newUri != null) {
                    isOk = UserNetworkController.signUpUser(data, newUri, SignUpActivity.this);
                    isCorrect = true;
                }
            } catch (Exception e) {
                isCorrect = false;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(isCorrect) {
                Toast.makeText(SignUpActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                if(isOk) {
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
    }
}
