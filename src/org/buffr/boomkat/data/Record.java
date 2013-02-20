package org.buffr.boomkat.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Record implements Parcelable {
    public String id;
    public String title;
    public String artist;
    public String label;
    public String genre; // TODO: ArrayList
    public String url;
    public String thumbnailUrl;

    public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
        public Record createFromParcel(Parcel in) {
            return new Record(in);
        }

        public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    public Record() {
    }

    private Record(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(artist);
        out.writeString(label);
        out.writeString(genre);
        out.writeString(url);
        out.writeString(thumbnailUrl);
    }

    public void readFromParcel(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        label = in.readString();
        genre = in.readString();
        url = in.readString();
        thumbnailUrl = in.readString();
    }
}
