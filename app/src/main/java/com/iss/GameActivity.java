package com.iss;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
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


public class GameActivity extends AppCompatActivity implements View.OnClickListener{
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
    private MediaPlayer clickSoundPlayer;
    private MediaPlayer gameOverSoundPlayer;


    private ArrayList<String> selectedImageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setCardImages();
        setRecyclerView();

        timerChronometer = findViewById(R.id.timer_view);
        scoreCounter = findViewById(R.id.score_counter);

        gameOverSoundPlayer = MediaPlayer.create(this, R.raw.smb_stage_clear);

        Button btnResult = findViewById(R.id.btnResult);
        btnResult.setOnClickListener(this);

        SaveScore = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + "won_time" + ".txt");

    }

    @Override
    public void onClick(View v) {
        clickSoundPlayer = MediaPlayer.create(v.getContext(), R.raw.smb_kick);
        clickSoundPlayer.setVolume(2.5f, 2.5f);
        clickSoundPlayer.start();

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


    private void setRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CardsAdapter(cards, recyclerView, getApplicationContext());
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
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
            Toast.makeText(this,"You won!", Toast.LENGTH_LONG).show();

            if((SystemClock.elapsedRealtime() - timerChronometer.getBase()) / 1000 < MaxScore){
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
                        if(Integer.parseInt(line) < value[0])
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




}
