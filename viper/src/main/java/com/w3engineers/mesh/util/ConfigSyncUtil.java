package com.w3engineers.mesh.util;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.model.ConfigSyncEvent;
import com.w3engineers.models.ConfigurationCommand;
import com.w3engineers.models.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class ConfigSyncUtil {

    private static ConfigSyncUtil configSyncUtil = null;
    private ConfigSyncCallback configSyncCallback;

    static {
        configSyncUtil = new ConfigSyncUtil();
    }

    private ConfigSyncUtil() {

    }

    public static ConfigSyncUtil getInstance() {
        return configSyncUtil;
    }

    public interface ConfigSyncCallback {
        void configSynced(boolean isUpdate, ConfigurationCommand configurationCommand);
    }

    public void setConfigSyncCallback(ConfigSyncCallback configSyncCallback) {
        this.configSyncCallback = configSyncCallback;
    }

    public void startConfigurationSync(Context context, boolean isMeshStartTime) {
        new ConfigurationTask(context, isMeshStartTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class ConfigurationTask extends AsyncTask<String, Void, String> {

        private Context context;
        private boolean isMeshStartTime;

        public ConfigurationTask(Context context, boolean isMeshStartTime) {
            this.context = context;
            this.isMeshStartTime = isMeshStartTime;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                String userName = SharedPref.read(Constant.PreferenceKeys.AUTH_USER_NAME);
                String userPass = SharedPref.read(Constant.PreferenceKeys.AUTH_PASSWORD);

                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, userPass.toCharArray());
                    }
                });

                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            processConfigJson(context, s, isMeshStartTime);
        }
    }

    private void processConfigJson(Context context, String configData, boolean isMeshStartTime) {

        ConfigurationCommand configurationCommand;

        if (!TextUtils.isEmpty(configData)) {
            configurationCommand = new Gson().fromJson(configData, ConfigurationCommand.class);
        } else {
            configData = loadJSONFromAsset(context);
            configurationCommand = new Gson().fromJson(configData, ConfigurationCommand.class);
        }

        int configVersion = PreferencesHelperDataplan.on().getConfigVersion();

        ConfigSyncEvent configSyncEvent = new ConfigSyncEvent();

        if (configurationCommand != null && configVersion < configurationCommand.getConfigVersionCode()) {

            PreferencesHelperDataplan.on().setConfigVersion(configurationCommand.getConfigVersionCode());
            PreferencesHelperDataplan.on().setPerMbTokenValue(configurationCommand.getTokenPerMb());

            for (Network network : configurationCommand.getNetwork()) {
                EthereumServiceUtil.getInstance(context).insertNetworkInfo(new NetworkInfo().toNetworkInfo(network));
            }

            configSyncEvent.setUpdate(true);
        } else {
            configSyncEvent.setUpdate(false);
        }
        configSyncEvent.setMeshStartTime(isMeshStartTime);
        configSyncEvent.setConfigurationCommand(configurationCommand);
        AppDataObserver.on().sendObserverData(configSyncEvent);
    }

    private String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("configuration.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
