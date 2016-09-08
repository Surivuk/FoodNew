package com.example.aleksandarx.foodfinder.network;

import android.content.Context;

import com.example.aleksandarx.foodfinder.data.model.FoodModel;
import com.example.aleksandarx.foodfinder.data.sqlite.DBAdapter;
import com.example.aleksandarx.foodfinder.data.sqlite.FoodTable;
import com.example.aleksandarx.foodfinder.settings.Connections;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Darko on 27.06.2016.
 */
public class HttpHelper {
    //private static String localServer = "http://192.168.1.7:8081/";
    //private static String herokuLive = localServer;//"http://food-finder-app.herokuapp.com/";
    //private static String server = herokuLive;
    public static String signUpHeroku(String username,String password) {
        String retStr = "";
        HttpURLConnection conn = null;
        try {

            conn = SetupConnection(Connections.liveServerURL,10000,15000,"POST","application/json; charset=UTF-8","application/json");

            //JSONObject holder = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("Username", username);
            data.put("Password", password);


            /*Uri.Builder builder = new Uri.Builder()
                    //.appendQueryParameter("req", SEND_MY_PLACE)
                    //.appendQueryParameter("name", "ime")
                    .appendQueryParameter("payload", data.toString());
            String query = builder.build().getEncodedQuery();*/

            OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());

            wr.write(data.toString());
            wr.close();

            /*OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes("UTF-8"));
            os.close();
            OutputStreamWriter wr= new OutputStreamWriter(os);
            wr.write(data.toString());*/
            /*BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(data.toString());
            writer.flush();
            writer.close();
            os.close();*/
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                retStr = inputStreamToString(conn.getInputStream());

            }
            else{
                retStr = String.valueOf("Error "+responseCode+" Data sent:"+data.toString());
            }
            //conn.connect();



            conn.disconnect();
            return retStr;

        } catch (Exception e) {
            e.printStackTrace();
            //conn.disconnect();

            return "Exception:"+e.getMessage()+"\n URL:";
        }finally {
            if(conn != null)
                conn.disconnect();
        }

    }

    public static String loginHeroku(String username, String password) {
        HttpURLConnection conn = null;
        String ret = "ERROR";
        try {
            conn = SetupConnection(Connections.liveServerURL + "signinMobile", 10000, 15000, "POST","application/json; charset=UTF-8","application/json");

            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("password", password);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(data.toString());
            wr.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                String reader = inputStreamToString(conn.getInputStream());
                JSONObject json = new JSONObject(reader);
                String id = json.getString("user_id");
                ret = id;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
        }
        return ret;
    }

    public static List<MarkerOptions> findPlacesAroundYou(double lat, double lng)
    {
        List<MarkerOptions> positions = new ArrayList<>();

        HttpURLConnection conn = null;
        try {
            conn = SetupConnection(Connections.liveServerURL+"placesAround",10000,15000,"POST","application/json; charset=UTF-8","application/json");

            JSONObject data = new JSONObject();
            data.put("lat", lat);
            data.put("lng", lng);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(data.toString());
            wr.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                String jsonContent =  inputStreamToString(conn.getInputStream());
                JSONArray places = new JSONArray(jsonContent);
                for(int i = 0 ; i < places.length(); i++)
                {
                    JSONObject place = places.getJSONObject(i);
                    double latitude = place.getDouble("restaurant_latitude");
                    double longitude = place.getDouble("restaurant_longitude");
                    String name = place.getString("restaurant_name");

                    MarkerOptions options = new MarkerOptions();
                    options.position(new LatLng(latitude,longitude));
                    options.title(name);

                    positions.add(options);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }finally {

            if(conn != null)
                conn.disconnect();
            return positions;
        }

    }

    public static boolean getMyFood(String id, Context context){
        HttpURLConnection conn = null;
        boolean ret = false;
        try {
            conn = SetupConnection(Connections.liveServerURL + "articlesMobile?id="+id, 10000, 15000, "POST", "-1", "application/json");
            //conn.setRequestProperty("id", id);

            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            String query = "id="+id;
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                String reader = inputStreamToString(conn.getInputStream());
                JSONArray array = new JSONArray(reader);
                DBAdapter db = DBAdapter.createAdapter(context);
                db.open();
                List<Object> tmpList = db.readAll();
                db.close();
                List<Long> ids = new ArrayList<>();
                for(int i = 0; i < tmpList.size(); i++){
                    FoodModel model = (FoodModel)tmpList.get(i);
                    ids.add(model.getDb_id());
                }
                for(int i = 0; i < array.length(); i++){
                    JSONObject json = array.getJSONObject(i);
                    boolean next = true;
                    Long tmp = json.getLong("article_id");
                    for(int j = 0; j < ids.size(); j++){
                        if(ids.get(j) == tmp) {
                            next = false;
                            break;
                        }
                    }
                    if(next){
                        FoodModel model = new FoodModel();
                        model.addItem("user_id", json.getString("article_user"));
                        model.addItem("articleName", json.getString("article_name"));
                        model.addItem("origin", json.getString("article_origin"));
                        model.addItem("foodType", json.getString("food_type"));
                        model.addItem("locationName", json.getString("restaurant_name"));
                        model.addItem("locationAddress", json.getString("restaurant_address"));
                        model.addItem("place_id", json.getString("restaurant_google_id"));
                        model.addItem("mealType", json.getString("meal_type"));
                        model.addItem("articleDescription", json.getString("article_description"));
                        model.setDb_id(Long.parseLong(json.getString("article_id")));
                        model.setArticle_image(Connections.liveServerURL + "image?id=" + json.getString("article_id"));
                        db.open();
                        db.insert(FoodTable.getTableName(), model);
                        db.close();
                    }
                }
                ret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
            return ret;
        }
    }

    public static String newFood(byte[] picture, HashMap<String, String> data)
    {
        String attachmentName = "imageFile";
        String attachmentFileName = "bitmap.png";
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        try{
            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(Connections.liveServerURL + "newFood");
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

            FoodModel model = new FoodModel();

            if(picture != null) {
                //************************************
                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                request.writeBytes(crlf);
                request.write(picture);
                request.writeBytes(crlf);
                //************************************
            }

            String[] tmpFields = FoodModel.FIELDS;
            for(int i = 0; i < tmpFields.length; i++){
                if(data.containsKey(tmpFields[i])){
                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" + tmpFields[i] + "\"" + crlf + crlf + data.get(tmpFields[i]) + crlf);
                }
            }

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"mobile\"" + crlf + crlf + "mobile" + crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

            /*request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"articleName\"" + crlf + crlf + data.getString("articleName") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"user_id\"" + crlf + crlf + "1" + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"isFood\"" + crlf + crlf + data.getString("isFood") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"mealType\"" + crlf + crlf + data.getString("mealType") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"foodType\"" + crlf + crlf + data.getString("foodType") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"origin\"" + crlf + crlf + data.getString("origin") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"locationName\"" + crlf + crlf + data.getJSONObject("location").getString("name") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"locationLat\"" + crlf + crlf + data.getJSONObject("location").getString("lat") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"locationLng\"" + crlf + crlf + data.getJSONObject("location").getString("lng") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"locationAddress\"" + crlf + crlf + data.getJSONObject("location").getString("vicinity") + crlf);

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"place_id\"" + crlf + crlf + data.getJSONObject("location").getString("place_id") + crlf);*/


            request.flush();
            request.close();

            int responseCode = httpUrlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                httpUrlConnection.disconnect();



                return "Successful";
            }

            httpUrlConnection.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "Failed";
        }

        return "Failed";
    }

    private static HttpURLConnection SetupConnection(String url,int readTimeout,int connectionTimeout,String method,String contentType,String accept) throws IOException {

        URL serverUrl = new URL(url);


        HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connectionTimeout);
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        //conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if(!contentType.equals("-1"))
            conn.setRequestProperty("Content-Type", contentType);
        //conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept", accept);

        return conn;
    }

    public static String inputStreamToString(InputStream is)
    {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try{
            while((line = rd.readLine()) != null)
            {
                total.append(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}
