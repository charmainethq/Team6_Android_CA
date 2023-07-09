package com.iss;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 123;
    private EditText urlEditText;
    private Button fetchButton;
    private GridView gridView;
    private ProgressBar progressBar;
    private ArrayList<String> imageUrls;
    private Thread downloadThread;
    private TextView progressText;
    private int count;

    private ArrayList<String> selectedImageUrls;
    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<String> imageUrls = intent.getStringArrayListExtra("imageUrls");
            int count = intent.getIntExtra("count", 0);
            gridView.setAdapter(new ImageAdapter(MainActivity.this, imageUrls));
            // if count above 20, show download completed and hide progress bar
            if(count >= 20){
                downloadCompleted(count);
                // Hide download progress bar after download completed
                // Delayed for 3 seconds to let 20 images load onto grid view
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                }, 2000); // 2s = 2000ms
            }
        }
    };

    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<String> imageUrls = intent.getStringArrayListExtra("imageUrls");
            count = intent.getIntExtra("count", 0);
            updateProgress(count);
            gridView.setAdapter(new ImageAdapter(MainActivity.this, imageUrls));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fetchButton = findViewById(R.id.fetchButton);
        gridView = findViewById(R.id.gridView);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        IntentFilter completeFilter = new IntentFilter(DownloadService.DOWNLOAD_COMPLETE);
        registerReceiver(completeReceiver, completeFilter);

        IntentFilter progressFilter = new IntentFilter(DownloadService.PROGRESS_UPDATE);
        registerReceiver(progressReceiver, progressFilter);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlEditText.getText().toString();
                if (!url.isEmpty()) {
                    // Check if the app has the required permissions
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Permissions are already granted, proceed with the download
                        hideKeyboard();
                        startDownload(url);

                    } else {
                        // Request permissions from the user
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                }

                selectedImageUrls = new ArrayList<>();

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ImageView imageView = (ImageView) view; // Assuming that the GridView items are ImageViews
                        String selectedImageUrl = imageUrls.get(position);

                        if (selectedImageUrls.contains(selectedImageUrl)) {
                            // The image is already selected, so deselect it
                            selectedImageUrls.remove(selectedImageUrl);
                            imageView.setBackgroundResource(0); // Remove the border
                        } else if (selectedImageUrls.size() < 6) { // Allow up to 6 images to be selected
                            // The image is not selected, so select it
                            selectedImageUrls.add(selectedImageUrl);
                            // TODO: Add borders
                        }

                        Toast.makeText(MainActivity.this, "Selected " + selectedImageUrls.size() + " of 6 images", Toast.LENGTH_SHORT).show();

                        if (selectedImageUrls.size() == 6) {
                            // When 6 images have been selected, launch GameActivity
                            launchGameActivity(view);
                        }
                    }
                });
            }
        });
    }

    public void launchGameActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putStringArrayListExtra("SelectedImages", selectedImageUrls);
        startActivity(intent);
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the download
                startDownload(urlEditText.getText().toString());
            } else {
                // Permission denied
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
        unregisterReceiver(progressReceiver);
    }

    private void startDownload(final String url) {
        showProgress();
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", url);
        startService(intent);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressText.setText("Downloading 0 of 20 images");
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void updateProgress(int count) {
        progressText.setText("Downloading " + count + " of 20 images");
        progressBar.setProgress(count);
        if (count == 20) {
            Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_SHORT).show();
        }
    }

    // download completed bar
    private void downloadCompleted(int count) {
        progressText.setText("Finished downloading 20 images");
        progressBar.setProgress(count);
    }


}
