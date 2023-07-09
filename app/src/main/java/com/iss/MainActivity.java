package com.iss;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fetchButton = findViewById(R.id.fetchButton);
        gridView = findViewById(R.id.gridView);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

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
                        downloadImages(url);

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

    private void downloadImages(final String url) {
        if (downloadThread != null && downloadThread.isAlive()) {
            downloadThread.interrupt();
        }

        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressText.setText("Downloading 0 of 20 images");
                    }
                });

                imageUrls = new ArrayList<>();
                count = 0;

                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements imgElements = doc.select("img");

                    for (Element imgElement : imgElements) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        String imageUrl = imgElement.absUrl("src");
                        Log.d("tag", imageUrl);

                        int minWidth = 500;
                        int minHeight = 500;
                        if (isImageDimensionsValid(imageUrl, minWidth, minHeight)) {
                            String imagePath = downloadImage(imageUrl); // this now returns the file path
                            imageUrls.add(imagePath); // save the file path instead of the URL
                            count++;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressText.setText("Downloading " + count + " of 20 images");
                                    progressBar.setProgress(count);
                                    gridView.setAdapter(new ImageAdapter(MainActivity.this, imageUrls));
                                }
                            });

                            // Break the loop after downloading 20 images
                            if (count >= 20) {
                                break;
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressText.setText("Downloaded " + count + " of 20 images");
                    }
                });
            }
        });

        downloadThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the download
                downloadImages(urlEditText.getText().toString());
            } else {
                // Permission denied
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isImageDimensionsValid(String imageUrl, int minWidth, int minHeight) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

            // Check if the image dimensions meet the criteria
            if (imageWidth >= minWidth && imageHeight >= minHeight) {
                return true;
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String downloadImage(String imageUrl) {
        String filePath = null;
        try {
            URL url = new URL(imageUrl);
            String fileName = sanitizeFileName(url.getFile());
            String newFileName = "game_" + fileName + ".jpg"; // Add the prefix

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            File file = new File(getExternalFilesDir(null), newFileName); // Use the new file name
            filePath = file.getAbsolutePath();

            FileOutputStream output = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }


    private String sanitizeFileName(String fileName) {
        // Remove invalid characters from the file name
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        return sanitizedFileName;
    }
}
