package com.iss;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
    private String imagePath;
    private boolean isFlipped = false;
    private boolean isMatched = false;
    public Card(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean getFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public boolean getMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    public String getImagePath() {
        return imagePath;
    }
    protected Card(Parcel in) {
        imagePath = in.readString();
        isFlipped = in.readByte() != 0;
        isMatched = in.readByte() != 0;
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imagePath);
        dest.writeByte((byte) (isFlipped ? 1 : 0));
        dest.writeByte((byte) (isMatched ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
