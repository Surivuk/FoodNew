package com.example.aleksandarx.foodfinder.network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darko on 03.09.2016.
 */
public class FriendModel implements Parcelable {
    public int ID;
    public double lat;
    public double lng;
    public String username;

    public FriendModel(int id, double lat, double lng,String usr)
    {
        this.ID = id;
        this.lat = lat;
        this.lng = lng;
        this.username = usr;
    }

    public FriendModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ID);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeString(this.username);
    }

    protected FriendModel(Parcel in) {
        this.ID = in.readInt();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.username = in.readString();
    }

    public static final Creator<FriendModel> CREATOR = new Creator<FriendModel>() {
        @Override
        public FriendModel createFromParcel(Parcel source) {
            return new FriendModel(source);
        }

        @Override
        public FriendModel[] newArray(int size) {
            return new FriendModel[size];
        }
    };
}
