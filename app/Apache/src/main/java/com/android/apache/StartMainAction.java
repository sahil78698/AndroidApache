package com.android.apache;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class StartMainAction {
    public static Context context;
    public static String CURRENT_GAME;
    public static String MOD_NAME;
    public static String DOWNLOADED_FILE;
    public static TextView percent;
    public static ProgressBar progressBar;

    public static LoaderTask task;

    public static void start(Context mContext, String current_game, String mod_name, String url, TextView per, ProgressBar progress) {
        context = mContext;
        CURRENT_GAME = current_game;
        MOD_NAME = mod_name;
        percent = per;
        progressBar = progress;

        task = new LoaderTask(context);
        task.execute(url);
    }

    public static void cancel() {
        task.cancel(true);
    }

    public static class LoaderTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock wakeLock;

        public LoaderTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }
                int filelength = connection.getContentLength();
                input = connection.getInputStream();
                if (!new File("/storage/emulated/0/BadSource/" + CURRENT_GAME + "/").exists()) {
                    new File("/storage/emulated/0/BadSource/" + CURRENT_GAME + "/").mkdirs();
                }
                output = new FileOutputStream("/storage/emulated/0/BadSource/" + CURRENT_GAME + "/" + MOD_NAME + ".zip");
                DOWNLOADED_FILE = "/storage/emulated/0/BadSource/" + CURRENT_GAME + "/" + MOD_NAME + ".zip";
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (filelength > 0) {
                        publishProgress((int) (total * 100 / filelength));
                    }
                    output.write(data, 0, count);
                }
            } catch (IOException e) {
                return e.toString();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire(10 * 60 * 1000L);
            percent.setText("Downloading...");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            percent.setText("Downloading " + values[0] + "%");
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            wakeLock.release();
            if (s != null) {
                System.out.println(s);
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "File Downloaded at " + DOWNLOADED_FILE, Toast.LENGTH_SHORT).show();
                percent.setText("Done");
            }
        }

        @Override
        protected void onCancelled() {
            wakeLock.release();
            Toast.makeText(context, "Download Cancelled", Toast.LENGTH_SHORT).show();
            super.onCancelled();
        }
    }

}
