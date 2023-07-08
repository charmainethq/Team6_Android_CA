package com.iss;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private Button fetchButton;
    private GridView gridView;
    private ProgressBar progressBar;

    private DownloadImagesTask downloadImagesTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fetchButton = findViewById(R.id.fetchButton);
        gridView = findViewById(R.id.gridView);
        progressBar = findViewById(R.id.progressBar);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlEditText.getText().toString();
                if (!url.isEmpty()) {
                    if (downloadImagesTask != null) {
                        downloadImagesTask.cancel(true);
                    }
                    downloadImagesTask = new DownloadImagesTask();
                    downloadImagesTask.execute(url);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void launchGameActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    private class DownloadImagesTask extends AsyncTask<String, Integer, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            String url = params[0];
            ArrayList<String> imageUrls = new ArrayList<>();

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder pageContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    pageContent.append(line);
                }
                reader.close();

                Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*[\"']([^\"']+)\\.(jpg|jpeg|png|gif)[\"'][^>]*>");
                Matcher matcher = pattern.matcher(pageContent.toString());

                int count = 0;
                while (matcher.find() && count < 20) {
                    String imageUrl = matcher.group(1) + "." + matcher.group(2);
                    if (!imageUrl.isEmpty()) {
                        downloadImage(imageUrl); // Download the image
                        imageUrls.add(imageUrl);
                        count++;
                        publishProgress(count);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return imageUrls;
        }

        private void downloadImage(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                // Create a new file with a unique name for each image
                String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                File file = new File(getExternalFilesDir(null), fileName);

                // Create a FileOutputStream to write the image data to the file
                FileOutputStream out = new FileOutputStream(file);

                // Read the image data from the connection's input stream and write it to the file
                InputStream in = conn.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                // Close the streams
                in.close();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            progressBar.setProgress(progress);
        }

        @Override
        protected void onPostExecute(ArrayList<String> imageUrls) {
            super.onPostExecute(imageUrls);
            progressBar.setVisibility(View.GONE);
            gridView.setAdapter(new ImageAdapter(MainActivity.this, imageUrls));
            Toast.makeText(MainActivity.this, "Images downloaded: " + imageUrls.size(), Toast.LENGTH_SHORT).show();
        }
    }
}
