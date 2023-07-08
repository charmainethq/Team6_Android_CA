package com.iss;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iss.GameActivity;
import com.iss.R;

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
                    // Check if the app has the required permissions
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Permissions are already granted, proceed with the download
                        downloadImagesTask = new DownloadImagesTask();
                        downloadImagesTask.execute(url);
                    } else {
                        // Request permissions from the user
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION);
                    }
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

    private class DownloadImagesTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            String url = strings[0];
            ArrayList<String> imageUrls = new ArrayList<>();
            int count = 0;

            try {
                Document doc = Jsoup.connect(url).get();
                Elements imgElements = doc.select("img");

                for (Element imgElement : imgElements) {
                    String imageUrl = imgElement.absUrl("src");
                    Log.d("tag", imageUrl);

                    // Filter the URLs based on image dimensions
                    // Adjust the minimum width and height as needed
                    int minWidth = 500;
                    int minHeight = 500;
                    if (isImageDimensionsValid(imageUrl, minWidth, minHeight)) {
                        imageUrls.add(imageUrl);
                        downloadImage(imageUrl);
                        count++;

                        // Break the loop after downloading 20 images
                        if (count >= 20) {
                            break;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return imageUrls;
        }

        private boolean isImageDimensionsValid(String imageUrl, int minWidth, int minHeight) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();

                // Use BitmapFactory to decode the image dimensions
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


        @Override
        protected void onPostExecute(ArrayList<String> imageUrls) {
            super.onPostExecute(imageUrls);
            progressBar.setVisibility(View.GONE);
            // Here you may want to update your GridView with downloaded images
        }

        private void downloadImage(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                String fileName = sanitizeFileName(url.getFile());
                String newFileName = "game2_" + fileName + ".jpg"; // Add the prefix

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                File file = new File(getExternalFilesDir(null), newFileName); // Use the new file name
                FileOutputStream output = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the download
                downloadImagesTask = new DownloadImagesTask();
                downloadImagesTask.execute(urlEditText.getText().toString());
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String sanitizeFileName(String fileName) {
        // Remove invalid characters from the file name
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        return sanitizedFileName;
    }
}
