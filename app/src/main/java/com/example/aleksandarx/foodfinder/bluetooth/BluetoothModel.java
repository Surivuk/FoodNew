package com.example.aleksandarx.foodfinder.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darko on 02.07.2016.
 */
public class BluetoothModel implements Parcelable {


    private double lat;
    private double lng;
    private String name;
    private String type;

    // No-arg Ctor
    public BluetoothModel(){}
    public BluetoothModel(double la,double lo,String n,String t)
    {
        lat = la;
        lng = lo;
        name = n;
        type = t;
    }
    // all getters and setters go here //...
    public void setLatLng(double latitude,double longitude)
    {
        this.lat = latitude;
        this.lng = longitude;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public double getLatitude()
    {
        return lat;
    }
    public double getLongitude()
    {
        return lng;
    }
    public String getName()
    {
        return name;
    }
    public String getType()
    {
        return type;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeDouble(lat);
        pc.writeDouble(lng);
        pc.writeString(name);
        pc.writeString(type);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<BluetoothModel> CREATOR = new Parcelable.Creator<BluetoothModel>() {
        public BluetoothModel createFromParcel(Parcel pc) {
            return new BluetoothModel(pc);
        }
        public BluetoothModel[] newArray(int size) {
            return new BluetoothModel[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    public BluetoothModel(Parcel pc){
        lat         = pc.readDouble();
        lng        =  pc.readDouble();
        name      = pc.readString();
        type = pc.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        String serialized = "";

        serialized += "Name: "+ name + "\n";
        serialized += "Type: "+ type + "\n";
        serialized += "Latitude: "+ String.valueOf(lat) + "\n";
        serialized += "Longitude: "+ String.valueOf(lng) + "\n";

        return serialized;
    }
}
