package com.w3engineers.mesh.application.ui.util;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.w3engineers.models.TokenGuideLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileStoreUtil {

    private static String FILE_NAME = "index.html";
    private static String GUIDELINE_FILE_NAME = "guideline.txt";
    private static String CHILD_PATH = "/Telemesh/web/";

    private static Storage storage;

    public static void writeWebFile(Context context, String content) {
        storage = new Storage(context);
        String path = storage.getExternalStorageDirectory() + CHILD_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        File defaultAddressFile = new File(file, FILE_NAME);

        try {
            defaultAddressFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(defaultAddressFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.append(content);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTokenGuideline(Context context, String guideline) {
        storage = new Storage(context);
        String path = storage.getExternalStorageDirectory() + CHILD_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        File defaultAddressFile = new File(file, GUIDELINE_FILE_NAME);

        try {
            defaultAddressFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(defaultAddressFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.append(guideline);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TokenGuideLine getGuideline(Context context) {
        storage = new Storage(context);
        String path = storage.getExternalStorageDirectory() + CHILD_PATH;
        File file = new File(path);
        File addressFile = new File(file, GUIDELINE_FILE_NAME);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(addressFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Gson().fromJson(text.toString(), TokenGuideLine.class);
    }

    public static String getWebFile() {
        String path = Environment.getExternalStorageState() + CHILD_PATH + FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            return "file://" + path;
        } else {
            return "file:///android_asset/default_page.html";
        }
    }

    public static boolean isWebFileExist() {
        String path = Environment.getExternalStorageState() + CHILD_PATH + FILE_NAME;
        File file = new File(path);
        return file.exists();
    }
}
