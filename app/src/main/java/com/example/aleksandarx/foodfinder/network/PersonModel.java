package com.example.aleksandarx.foodfinder.network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darko on 03.07.2016.
 */
public class PersonModel implements Parcelable {
    public String connectionID;
    public String foodType;
    public double latitude;
    public double longitude;
    public int ID;
    public PersonModel(String cid,String ft,double lat,double lng,int id)
    {
        connectionID = cid;
        foodType = ft;
        latitude = lat;
        longitude = lng;
        ID = id;
    }

    @Override
    public String toString() {
        String json = "{";
        json += "\"connectionID\":\""+connectionID+"\"";
        json += ",\"foodType\":\""+foodType+"\"";
        json += ",\"latitude\":\""+String.valueOf(latitude)+"\"";
        json += ",\"longitude\":\""+String.valueOf(longitude)+"\"";
        json += "}";
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.connectionID);
        dest.writeString(this.foodType);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.ID);
    }

    protected PersonModel(Parcel in) {
        this.connectionID = in.readString();
        this.foodType = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.ID = in.readInt();
    }

    public static final Creator<PersonModel> CREATOR = new Creator<PersonModel>() {
        @Override
        public PersonModel createFromParcel(Parcel source) {
            return new PersonModel(source);
        }

        @Override
        public PersonModel[] newArray(int size) {
            return new PersonModel[size];
        }
    };
}
