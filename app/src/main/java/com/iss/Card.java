package com.iss;

public class Card {
    public int image; // for drawable implementation, to be removed
    Card(int image) {
        this.image = image;
    } // for drawable implementation, to be removed

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
}