package com.iss;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GameActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CardsAdapter adapter;

    public List<Card> cards;
    public static Card selectedCard = null;
    private TextView scoreCounter;
    private int score = 0;

    boolean isGameStarted = false;

    private Chronometer timerChronometer;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //For testing
        //int[] cardImages = {R.drawable.image1};

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

        scoreCounter = findViewById(R.id.score_counter);
        updateScore();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CardsAdapter(cards);
        recyclerView.setAdapter(adapter);
    }

    public void incrementScore() {
        score++;
        updateScore();
    }
    private void updateScore() {
        scoreCounter.setText("Score: " + score + "/6");
        checkGameOver();
    }



    public void startTimer() {
        if (!isGameStarted) {
            timerChronometer = findViewById(R.id.timer_view);
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
            isGameStarted = true;
        }
    }
    public void checkGameOver() {
        if (score == 6) {
            timerChronometer.stop();
            // Show game over dialog or something
            Toast.makeText(this,"You won!", Toast.LENGTH_LONG).show();
        }
    }




}
