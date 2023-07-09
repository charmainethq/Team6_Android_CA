package com.iss;

public class Card {
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
