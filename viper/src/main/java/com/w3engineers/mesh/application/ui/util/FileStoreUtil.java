package com.w3engineers.mesh.application.ui.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileStoreUtil {

    private static String FILE_NAME = "index.html";
    private static String CHILD_PATH = "/Telemesh/web/";

    public static void writeWebFile(Context context, String content) {
        String path = Environment.getExternalStorageState() + CHILD_PATH;
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
