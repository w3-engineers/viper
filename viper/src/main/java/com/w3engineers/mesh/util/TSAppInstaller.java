package com.w3engineers.mesh.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.w3engineers.ext.strom.App;
import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.databinding.DialogServiceAppInstallProgressBinding;
import com.w3engineers.mesh.util.lib.mesh.HandlerUtil;
import com.w3engineers.mesh.util.lib.remote.RetrofitInterface;
import com.w3engineers.mesh.util.lib.remote.RetrofitService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */


public class TSAppInstaller {
    private static DownloadZipFileTask downloadZipFileTask;
    private static final String TAG = "appDownloadTest";
    public static boolean isAppUpdating;
    private static DialogServiceAppInstallProgressBinding binding;
    private static AlertDialog dialog;

    public static void downloadApkFile(Context context, String baseUrl) {


        showDialog(context);

        Log.d(TAG, "File url: " + baseUrl);

        RetrofitInterface downloadService = RetrofitService.createService(RetrofitInterface.class, baseUrl);
        Call<ResponseBody> call = downloadService.downloadFileByUrl("Service.apk");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got response body");

                    downloadZipFileTask = new DownloadZipFileTask(context);
                    downloadZipFileTask.execute(response.body());

                } else {
                    Log.d(TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                closeDialog(t.getMessage());
                Log.e(TAG, t.getMessage());
                //   isAppUpdating = false;
                //  InAppUpdate.getInstance(App.getContext()).setAppUpdateProcess(false);
            }
        });
    }


    private static class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {
        private Context context;

        public DownloadZipFileTask(Context context) {
            this.context = context;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], "right_mesh_service.apk");
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d("API123", progress[0].second + " ");

            if (progress[0].first == 100) {
                Toaster.showShort("File downloaded successfully");
            }


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                //progressBar.setProgress(currentProgress);
                binding.progressBar.setProgress(currentProgress);

                // txtProgressPercent.setText("Progress " + currentProgress + "%");
            }

            if (progress[0].first == -1) {
                closeDialog("Download failed");
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "right_mesh_service.apk");

                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String packageName = "com.w3engineers.unicef.telemesh.provider";
//                    packageName = "com.w3engineers.ext.viper.provider";
                    Uri apkUri = FileProvider.getUriForFile(context,  packageName, destinationFile);
                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    Log.d("InAppUpdateTest", "app uri: " + apkUri.getPath());
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.d("InAppUpdateTest", "app install process start");
                } else {
                    Uri apkUri = Uri.fromFile(destinationFile);
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                closeDialog("Download completed");
                context.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                closeDialog(e.getMessage());
            }


            //    isAppUpdating = false;
            //    InAppUpdate.getInstance(App.getContext()).setAppUpdateProcess(false);
        }
    }

    private static void saveToDisk(ResponseBody body, String filename) {
        try {
            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                //  Log.d(TAG, destinationFile.getParent());

                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                closeDialog("Failed to save the file!");
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeDialog("Failed to save the file!");
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

    private static void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_service_app_install_progress, null, false);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private static void closeDialog(String message) {
        HandlerUtil.postForeground(()-> {
            Toaster.showShort(message);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });
    }
}
