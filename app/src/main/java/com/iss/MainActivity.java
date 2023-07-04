package com.iss;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CardsAdapter adapter;

    public static List<Card> cards;
    public static Card selectedCard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: replace with downloaded images somehow
        int[] cardImages = {R.drawable.image1, R.drawable.image2, R.drawable.image3,
                R.drawable.image4, R.drawable.image5, R.drawable.image6};

        // Add each image twice
        cards = new ArrayList<>();
        for (int cardImage : cardImages) {
            cards.add(new Card(cardImage));
            cards.add(new Card(cardImage));
        }
        Collections.shuffle(cards);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CardsAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

    // TODO: Unsure if should keep as static or not due to downloads?
    public static class Card {
        int image;
        boolean isFlipped = false;
        boolean isMatched = false;

        Card(int image) {
            this.image = image;
        }
    }
}
