package com.iss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer clickSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchGameActivity(View view) {
        clickSoundPlayer = MediaPlayer.create(view.getContext(), R.raw.smb_kick);
        clickSoundPlayer.setVolume(2.5f, 2.5f);
        clickSoundPlayer.start();
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }


}
