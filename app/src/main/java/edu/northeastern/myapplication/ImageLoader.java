package edu.northeastern.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // <-- Import the Android Log class
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    private static final String TAG = "ImageLoader"; // We'll use this tag for logging

    // Simple in-memory cache to avoid re-downloading the same images repeatedly
    private static Map<String, Bitmap> bitmapCache = new HashMap<>();

    public static void loadImageAsync(final String imageUrl, final ImageView imageView) {
        // If already cached, set it immediately
        if (bitmapCache.containsKey(imageUrl)) {
            Log.d(TAG, "Image is already in cache: " + imageUrl);
            imageView.setImageBitmap(bitmapCache.get(imageUrl));
            return;
        }

        // Otherwise, fetch on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    Log.d(TAG, "Starting download: " + imageUrl);
                    URL url = new URL(imageUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "Response code for " + imageUrl + ": " + responseCode);

                    // Get content type to see if it's actually an image (optional)
                    String contentType = connection.getContentType();
                    Log.d(TAG, "Content-Type for " + imageUrl + ": " + contentType);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Cache it
                        bitmapCache.put(imageUrl, bitmap);
                        Log.d(TAG, "Bitmap downloaded & cached for: " + imageUrl);

                        // Update ImageView on main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Setting image on ImageView for: " + imageUrl);
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        // If the response code is not 200, log it as an error
                        Log.e(TAG, "Failed to download image from: " + imageUrl
                                + " | Response code: " + responseCode);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while downloading image: " + imageUrl, e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception ignored) {}
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
