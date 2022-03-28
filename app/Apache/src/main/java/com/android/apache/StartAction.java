package com.android.apache;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartAction {
    public static Context context;
    public static ProgressBar progressBar;
    public static boolean canceled = false;
    public static TextView parsent;

    public static void cancel() {
        canceled = true;
    }

    public static void start(Context mContext, ProgressBar mProgressBar, String mod_name, String url, TextView per) {
        context = mContext;
        progressBar = mProgressBar;
        parsent = per;
        loader(context, mod_name, ".zip", url);
    }

    private static final int UPDATE_DOWNLOAD_PROGRESS = 1;

    public static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void loader(Context context, String fileName, String fileExtension, String url) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Downloading File");
            request.setMimeType("application/zip");
            request.allowScanningByMediaScanner();
            request.setAllowedOverMetered(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + fileExtension);
            long downloadId = downloadManager.enqueue(request);
            executor.execute(() -> {
                int progress = 0;
                boolean isDownloadFinished = false;
                while (!isDownloadFinished) {
                    Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        switch (downloadStatus) {
                            case DownloadManager.STATUS_RUNNING:
                                @SuppressLint("Range") long totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                if (totalBytes > 0) {
                                    @SuppressLint("Range") long downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                    progress = (int) (downloadedBytes * 100 / totalBytes);
                                }
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                progress = 100;
                                isDownloadFinished = true;
                                break;
                            case DownloadManager.STATUS_PAUSED:
                            case DownloadManager.STATUS_PENDING:
                                break;
                            case DownloadManager.STATUS_FAILED:
                                isDownloadFinished = true;
                                break;
                        }
                        Message message = Message.obtain();
                        message.what = UPDATE_DOWNLOAD_PROGRESS;
                        message.arg1 = progress;
                        mainHandler.sendMessage(message);
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e, Toast.LENGTH_SHORT).show();
            Log.e("Download Error", e.toString());
            Log.e("Download Error", e.getMessage());
        }
    }


    public static final Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == UPDATE_DOWNLOAD_PROGRESS) {
                if (canceled) {
                    executor.shutdown();
                    mainHandler.removeCallbacksAndMessages(null);
                }
                int downloadProgress = msg.arg1;
                progressBar.setProgress(downloadProgress);
                parsent.setText("Downloading " + downloadProgress);
                if (downloadProgress == 100) {

                    executor.shutdown();
                    mainHandler.removeCallbacksAndMessages(null);
                    Toast.makeText(context, "File Downloaded at " + Environment.DIRECTORY_DOWNLOADS + "/downloadchecker.zip", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    });

}
