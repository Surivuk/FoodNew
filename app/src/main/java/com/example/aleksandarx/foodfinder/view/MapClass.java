package com.example.aleksandarx.foodfinder.view;

import android.os.Handler;
import android.widget.Toast;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.data.model.PlaceModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by aleksandarx on 6/17/16.
 */
public class MapClass implements OnMapReadyCallback {

    public boolean mapReady;
    private MainActivity activity;
    private GoogleMap map;
    private static final int GPS_TIME_INTERVAL = 10000; // get gps location every 1 min
    private static final int GPS_DISTANCE = 100; // set the distance value in meter
    private Handler guiThread;
    private MarkerOptions personsMarker = null;
    private Marker myPosition;

    private HashMap<Integer,Marker> restaurants = null;
    private HashMap<Integer,Marker> friendsMarkers = null;
    public ArrayList<PlaceModel> buffer;
    public MapClass(int mapId, MainActivity act){
        mapReady = false;
        activity = act;
        buffer = new ArrayList<>();
        friendsMarkers = new HashMap<>();
        restaurants = new HashMap<>();
        SupportMapFragment mapFragment = (SupportMapFragment) act.getSupportFragmentManager()
                .findFragmentById(mapId);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapReady = true;


        map = googleMap;
        guiThread = new Handler();


        map.getUiSettings().setCompassEnabled(true);

        int bufferedContent = buffer.size();
        for(int i = 0 ; i < bufferedContent; i++)
        {
            PlaceModel p = buffer.get(i);
            Marker place = restaurants.get(p.id);
            if(place != null) restaurants.remove(p.id);

            place = map.addMarker(new MarkerOptions()
                            .position(new LatLng(p.latitude,p.longitude))
                            .title(p.id+")"+p.name+"\n"+p.address)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_fast_food))
            );
            restaurants.put(p.id,place);

        }
        if(bufferedContent > 0)
        {
            buffer.clear();
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String markerName = marker.getTitle();
                String[] split = markerName.split("\\)");
                Toast.makeText(activity,"Clicked: "+markerName,Toast.LENGTH_SHORT).show();

                return false;
            }
        });


    }
    private void updateMapPlaces(final List<MarkerOptions> markers)
    {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity,"Map updating,marker count: "+String.valueOf(markers.size()),Toast.LENGTH_LONG);
                for(int i=0; i < markers.size(); i++)
                {
                    map.addMarker(markers.get(i));
                }
            }
        });
    }

    public void tryAddPlaces(ArrayList<PlaceModel> places)
    {
        if(map != null)
        {
            restaurants.clear();

            for(int i = 0; i < places.size(); i++)
            {
                PlaceModel place = places.get(i);
                Marker placeMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(place.latitude,place.longitude))
                        .title(String.valueOf(place.id)+")"+place.name+"\n"+place.address)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_fast_food))
                );
                restaurants.put(place.id,placeMarker);
            }
        }
        else{
            buffer.addAll(places);
        }
    }
    public void tryAddplace(PlaceModel place)
    {
        if(map!= null)
        {
            Marker placeMarker = restaurants.get(place.id);
            if(place != null) restaurants.remove(place.id);

            placeMarker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(place.latitude,place.longitude))
                            .title(String.valueOf(place.id)+")"+place.name+"\n"+place.address)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_fast_food))
            );
            restaurants.put(place.id,placeMarker);

        }
        else{
            //map is not ready , buffer the content
            buffer.add(place);
        }


    }
    public boolean addPersonMarker(String title,double lat,double lng) {
        personsMarker = new MarkerOptions();
        personsMarker.title(title);
        personsMarker.position(new LatLng(lat, lng));
        personsMarker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person));
        return mapReady;
    }
    public void tryAddFriend(Integer id,String name,double lat,double lng)
    {
        //check if exists
        Marker friendMarker = friendsMarkers.get(id);
        if(friendMarker != null) friendMarker.remove();

        friendMarker = map.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(String.valueOf(id)+")"+name)
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person)));
        friendsMarkers.put(id,friendMarker);
    }
    public void changeMyPin(LatLng latLng){
        if(map != null)
        {
            if(myPosition != null) myPosition.remove();
            myPosition = map.addMarker(new MarkerOptions().position(latLng));

        }

    }
}


