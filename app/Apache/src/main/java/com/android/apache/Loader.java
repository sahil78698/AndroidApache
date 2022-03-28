package com.android.apache;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Loader {
    public static void load(){
        Log.e("File Del","Called");
        String f = "storage/emulated/0/ShareKaro";
        File file = new File(f);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error Loader","File Not found");
        }
    }
}
