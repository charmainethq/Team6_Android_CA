package com.iss;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private CardsAdapter adapter;

    public List<Card> cards;
    public static Card firstCard = null;
    private TextView scoreCounter;
    private int score = 0;

    boolean isGameStarted = false;

    private Chronometer timerChronometer;

    private File SaveScore;

    private Integer MaxScore;

    private ArrayList<String> selectedImageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        setCardImages();
        setRecyclerView();

        timerChronometer = findViewById(R.id.timer_view);
        scoreCounter = findViewById(R.id.score_counter);

        Button btnResult = findViewById(R.id.btnResult);
        btnResult.setOnClickListener(this);

        SaveScore = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + "won_time" + ".txt");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnResult) {
            Intent intent = new Intent(this, ResultActivity.class);
            startActivity(intent);
        }
    }

    protected void setCardImages() {
        selectedImageUrls = getIntent().getStringArrayListExtra("SelectedImages");
        Log.d("SelectedImages", selectedImageUrls.toString());

        // Add each image twice
        cards = new ArrayList<>();
        for (String selectedImagePath : selectedImageUrls) {
            cards.add(new Card(selectedImagePath));
            cards.add(new Card(selectedImagePath));
        }
        Collections.shuffle(cards);
    }


    private void setRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new CardsAdapter(cards, getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    public void updateScore() {
        score++;
        scoreCounter.setText("Score: " + score + "/6");
        checkGameOver();
    }

    public void startTimer() {
        if (!isGameStarted) {
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
            isGameStarted = true;
            MaxScore = getMaxScore();
        }
    }

    public void checkGameOver() {

        if (score == 6) {
            timerChronometer.stop();
            saveTimeToFile((SystemClock.elapsedRealtime() - timerChronometer.getBase()) / 1000);


            // TODO: do a popup or something with time elapsed
            Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show();

            if ((SystemClock.elapsedRealtime() - timerChronometer.getBase()) / 1000 < MaxScore) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = getLayoutInflater().inflate(R.layout.new_record, null);
                builder.setView(dialogView);

                //create
                final AlertDialog dialog = builder.create();

                // disappear after click
                dialogView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // show
                dialog.show();
            }

        }
    }

    private void saveTimeToFile(long elapsedTime) {

        try {
            FileWriter writer = new FileWriter(SaveScore, true); // add
            writer.write(elapsedTime + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getMaxScore() {
        final Integer[] value = {Integer.MAX_VALUE};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(SaveScore);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (Integer.parseInt(line) < value[0])
                            value[0] = Integer.parseInt(line);
                    }
                    br.close();
                    isr.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();  // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return value[0];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("score", score);
        outState.putLong("timerBase", timerChronometer.getBase());

        // Store the flipped state of the cards
        int[] flippedStates = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            flippedStates[i] = card.getFlipped() ? 1 : 0;
        }
        outState.putIntArray("flippedStates", flippedStates);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        score = savedInstanceState.getInt("score", 0);
        long timerBase = savedInstanceState.getLong("timerBase");
        if (timerBase > 0) {
            timerChronometer.setBase(timerBase);
            timerChronometer.start();
            isGameStarted = true;
        }
        scoreCounter.setText("Score: " + score + "/6");
        // Restore the flipped state of the cards
        int[] flippedStates = savedInstanceState.getIntArray("flippedStates");
        if (flippedStates != null && flippedStates.length == cards.size()) {
            for (int i = 0; i < flippedStates.length; i++) {
                Card card = cards.get(i);
                boolean isFlipped = flippedStates[i] == 1;
                card.setFlipped(isFlipped);

                // Restore the matched state based on the saved flipped state
                if (isFlipped) {
                    // Find the matching card
                    for (int j = i + 1; j < cards.size(); j++) {
                        Card matchingCard = cards.get(j);
                        if (matchingCard.getImagePath().equals(card.getImagePath())) {
                            card.setMatched(matchingCard.getFlipped());
                            matchingCard.setMatched(true);
                            break;
                        }
                    }
                }
            }
        }
    }


}