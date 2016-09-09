package com.example.aleksandarx.foodfinder.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darko on 08.09.2016.
 */
public class PlaceModel implements Parcelable {

    public int id;
    public String googleId;
    public String name;
    public double latitude;
    public double longitude;
    public String address;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.googleId);
        dest.writeString(this.name);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.address);
    }

    public PlaceModel() {
    }

    protected PlaceModel(Parcel in) {
        this.id = in.readInt();
        this.googleId = in.readString();
        this.name = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<PlaceModel> CREATOR = new Parcelable.Creator<PlaceModel>() {
        @Override
        public PlaceModel createFromParcel(Parcel source) {
            return new PlaceModel(source);
        }

        @Override
        public PlaceModel[] newArray(int size) {
            return new PlaceModel[size];
        }
    };
}
