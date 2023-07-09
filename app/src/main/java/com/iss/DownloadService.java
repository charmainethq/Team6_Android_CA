package com.iss;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
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

public class DownloadService extends Service {

    public static final String DOWNLOAD_COMPLETE = "com.iss.DOWNLOAD_COMPLETE";
    public static final String PROGRESS_UPDATE = "com.iss.PROGRESS_UPDATE";


    private ArrayList<String> imageUrls;
    private Thread downloadThread;
    private int count;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        startDownload(url);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed to be bound, so return null
        return null;
    }

    private void startDownload(final String url) {
        if (downloadThread != null && downloadThread.isAlive()) {
            downloadThread.interrupt();
        }

        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
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

                            Intent progressIntent = new Intent(PROGRESS_UPDATE);
                            progressIntent.putExtra("count", count);
                            progressIntent.putExtra("imageUrls", imageUrls);
                            sendBroadcast(progressIntent);

                            // Break the loop after downloading 20 images
                            if (count >= 20) {
                                break;
                            }
                        }
                    }

                    Intent completeIntent = new Intent(DOWNLOAD_COMPLETE);
                    completeIntent.putStringArrayListExtra("imageUrls", imageUrls);
                    sendBroadcast(completeIntent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        downloadThread.start();
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
