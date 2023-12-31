package com.iss;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DownloadService extends Service {

    public static final String DOWNLOAD_COMPLETE = "com.iss.DOWNLOAD_COMPLETE";
    public static final String PROGRESS_UPDATE = "com.iss.PROGRESS_UPDATE";
    public static final String ACTION_DOWNLOAD_ERROR = "com.iss.ACTION_DOWNLOAD_ERROR";
    public static final String EXTRA_ERROR_MESSAGE = "com.iss.EXTRA_ERROR_MESSAGE";



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
                    // Check if entered field  is a URL
                    URL urlObj = new URL(url);
                    String protocol = urlObj.getProtocol();
                    if (!protocol.equals("http") && !protocol.equals("https")) {
                        sendErrorBroadcast("Invalid URL protocol. Only HTTP and HTTPS are supported.");
                        return;
                    }

                    // Retrieve images
                    Document doc = Jsoup.connect(url).get();
                    Elements imgElements = doc.select("img");

                    if (imgElements.isEmpty()) {
                        sendErrorBroadcast("No images found at the provided URL.");
                        return;
                    }


                    for (Element imgElement : imgElements) {
                        synchronized (this) {
                            // Check here before starting the new loop iteration
                            if (Thread.currentThread().isInterrupted() || count >= 20) {
                                return;
                            }
                        }

                        String imageUrl = imgElement.absUrl("src");
                        Log.d("tag", imageUrl);

                        int minWidth = 350;
                        int minHeight = 280;

                        if (isImageDimensionsValid(imageUrl, minWidth, minHeight)) {
                            synchronized (this) {
                                // Download the image.
                                String imagePath = downloadImage(imageUrl);
                                if (Thread.currentThread().isInterrupted() || count >= 20) {
                                    return;
                                }
                                if (imagePath!=null && !imagePath.trim().isEmpty()){
                                    imageUrls.add(imagePath);
                                    count++;
                                    Log.d("count", "sankalp" + count);
                                    updateProgress(count);

                                }
                                if (count == 20){
                                    Log.d("tag", "here");
                                    Intent completeIntent = new Intent(DOWNLOAD_COMPLETE);
                                    completeIntent.putExtra("count", count);
                                    completeIntent.putStringArrayListExtra("imageUrls", imageUrls);
                                    sendBroadcast(completeIntent);
                                }
                            }

                        }
                    }
                    if (imageUrls.isEmpty()) {
                        sendErrorBroadcast("No images of suitable dimensions found at the provided URL.");
                        return;
                    }


                } catch (MalformedURLException e) {
                    sendErrorBroadcast("Please provide a valid URL.");
                    e.printStackTrace();
                } catch (IOException e) {
                    sendErrorBroadcast("Failed to connect to the provided URL.");
                    e.printStackTrace();
                }
            }
        });

        downloadThread.start();
    }

    private void sendErrorBroadcast(String errorMessage) {
        Intent errorIntent = new Intent(ACTION_DOWNLOAD_ERROR);
        errorIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
    }


    private void updateProgress(int count) {
        Intent progressIntent = new Intent(PROGRESS_UPDATE);
        progressIntent.putStringArrayListExtra("imageUrls", imageUrls); // Add this line
        progressIntent.putExtra("count", count);
        sendBroadcast(progressIntent);
    }

    private boolean isImageDimensionsValid(String imageUrl, int minWidth, int minHeight) {
        try {
            String encodedUrl = URLEncoder.encode(imageUrl, "UTF-8");
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");

            connection.connect();

            InputStream input = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            Log.d("Image Dimensions", "Width: " + imageWidth + ", Height: " + imageHeight);
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
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");

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
