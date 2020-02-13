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
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.w3engineers.mesh.application.data.AppDataObserver;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.model.ConfigSyncEvent;
import com.w3engineers.mesh.application.ui.util.FileStoreUtil;
import com.w3engineers.models.ConfigurationCommand;
import com.w3engineers.models.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        String downloadLink = SharedPref.read(Constant.PreferenceKeys.APP_DOWNLOAD_LINK) + "configuration.json";
        new ConfigurationTask(context, isMeshStartTime, downloadLink)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class ConfigurationTask extends AsyncTask<String, Void, String> {

        private Context context;
        private boolean isMeshStartTime;
        private String serverUrl;

        public ConfigurationTask(Context context, boolean isMeshStartTime, String url) {
            this.context = context;
            this.isMeshStartTime = isMeshStartTime;
            this.serverUrl = url;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(serverUrl);
                connection = (HttpURLConnection) url.openConnection();

                String userName = SharedPref.read(Constant.PreferenceKeys.AUTH_USER_NAME);
                String userPass = SharedPref.read(Constant.PreferenceKeys.AUTH_PASSWORD);

                String authString = (userName + ":" + userPass);
                byte[] data1 = authString.getBytes(UTF_8);
                String base64 = Base64.encodeToString(data1, Base64.NO_WRAP);


               /* Log.e("HttpError", "Credential " +userName+" password: "+userPass);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, userPass.toCharArray());
                    }
                });*/

                connection.setRequestProperty("Authorization", "Basic " + base64);

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

            MeshLog.v("config " + s);

            processConfigJson(context, s, isMeshStartTime);
        }
    }

    private void processConfigJson(Context context, String configData, boolean isMeshStartTime) {

        ConfigurationCommand configurationCommand = null;

        if (!TextUtils.isEmpty(configData)) {
            configurationCommand = new Gson().fromJson(configData, ConfigurationCommand.class);
        }

//        else {
//           //  configData = loadJSONFromAsset(context);
//
//            configData = SharedPref.read(Constant.PreferenceKeys.CONFIG_FILE);
//
//            configurationCommand = new Gson().fromJson(configData, ConfigurationCommand.class);
//        }

        ConfigSyncEvent configSyncEvent = new ConfigSyncEvent();
        if (configurationCommand != null) {
            MeshLog.v("configurationCommand " + configurationCommand.getConfigVersionName());

            int configVersion = PreferencesHelperDataplan.on().getConfigVersion();
            int tokenGuideVersion = PreferencesHelperDataplan.on().getTokenGuideVersion();

            if (configVersion < configurationCommand.getConfigVersionCode()) {

                if (tokenGuideVersion < configurationCommand.getTokenGuideVersion()) {
                    String downloadLink = SharedPref.read(Constant.PreferenceKeys.APP_DOWNLOAD_LINK) + "point_guide.json";
                    new DownloadGuidelineContent(context).execute(downloadLink);

                    PreferencesHelperDataplan.on().setTokenGuideVersion(configurationCommand.getTokenGuideVersion());
                }

                PreferencesHelperDataplan.on().setConfigVersion(configurationCommand.getConfigVersionCode());
                PreferencesHelperDataplan.on().setPerMbTokenValue(configurationCommand.getTokenPerMb());

                PreferencesHelperDataplan.on().setMaxPointForRmesh(configurationCommand.getMaxPointForRmesh());
                PreferencesHelperDataplan.on().setRmeshPerPoint(configurationCommand.getRmeshPerToken());
                SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_LINK, configurationCommand.getGiftDonateLink());

                SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_USERNAME, configurationCommand.getGiftDonateLink());
                SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_PASS, configurationCommand.getGiftDonatePass());
                SharedPref.write(Constant.PreferenceKeys.TX_HISTORY_URL_KOTTI, configurationCommand.getHistoryUrlKotti());
                SharedPref.write(Constant.PreferenceKeys.TX_URL_ROPSTEN, configurationCommand.getRopstenUrl());


                PreferencesHelperDataplan.on().setWalletRmeshAvailable(configurationCommand.isWalletRmeshAvailable());
                PreferencesHelperDataplan.on().setRmeshInfoText(configurationCommand.getRmeshInfoText());
                PreferencesHelperDataplan.on().setRmeshOwnerAddress(configurationCommand.getRmeshOwnerAddress());
                PreferencesHelperDataplan.on().setMainnetNetworkType(configurationCommand.getMainNetNetworkType());


                for (Network network : configurationCommand.getNetwork()) {
                    EthereumServiceUtil.getInstance(context).insertNetworkInfo(new NetworkInfo().toNetworkInfo(network));
                }

                configSyncEvent.setUpdate(true);

                EthereumServiceUtil.getInstance(context).getEthereumService().setGIftDonateUrl(configurationCommand.getGiftDonateLink());
            } else {
                EthereumServiceUtil.getInstance(context).getEthereumService().setGIftDonateUrl(SharedPref.read(Constant.PreferenceKeys.GIFT_DONATE_LINK));
                configSyncEvent.setUpdate(false);
            }
        } else {
            EthereumServiceUtil.getInstance(context).getEthereumService().setGIftDonateUrl(SharedPref.read(Constant.PreferenceKeys.GIFT_DONATE_LINK));
            configSyncEvent.setUpdate(false);
        }


        configSyncEvent.setMeshStartTime(isMeshStartTime);
        configSyncEvent.setConfigurationCommand(configurationCommand);
        AppDataObserver.on().sendObserverData(configSyncEvent);

    }

    public void loadFirstTimeData(Context context, String configData) {
        int configVersion = PreferencesHelperDataplan.on().getConfigVersion();

        //   String configData = loadJSONFromAsset(context);


        Log.e("config_file", "config_data:: " + configData);

        ConfigurationCommand configurationCommand = new Gson().fromJson(configData, ConfigurationCommand.class);

        if (configurationCommand != null && configVersion < configurationCommand.getConfigVersionCode()) {

            PreferencesHelperDataplan.on().setConfigVersion(configurationCommand.getConfigVersionCode());
            PreferencesHelperDataplan.on().setPerMbTokenValue(configurationCommand.getTokenPerMb());

            PreferencesHelperDataplan.on().setMaxPointForRmesh(configurationCommand.getMaxPointForRmesh());
            PreferencesHelperDataplan.on().setRmeshPerPoint(configurationCommand.getRmeshPerToken());
            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_LINK, configurationCommand.getGiftDonateLink());

            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_USERNAME, configurationCommand.getGiftDonateLink());
            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_PASS, configurationCommand.getGiftDonatePass());
            SharedPref.write(Constant.PreferenceKeys.TX_HISTORY_URL_KOTTI, configurationCommand.getHistoryUrlKotti());
            SharedPref.write(Constant.PreferenceKeys.TX_URL_ROPSTEN, configurationCommand.getRopstenUrl());

            PreferencesHelperDataplan.on().setWalletRmeshAvailable(configurationCommand.isWalletRmeshAvailable());
            PreferencesHelperDataplan.on().setRmeshInfoText(configurationCommand.getRmeshInfoText());
            PreferencesHelperDataplan.on().setRmeshOwnerAddress(configurationCommand.getRmeshOwnerAddress());
            PreferencesHelperDataplan.on().setMainnetNetworkType(configurationCommand.getMainNetNetworkType());

            for (Network network : configurationCommand.getNetwork()) {
                EthereumServiceUtil.getInstance(context).insertNetworkInfo(new NetworkInfo().toNetworkInfo(network));
            }
        }
    }

    public void updateConfigCommandFile(Context context, ConfigurationCommand configurationCommand) {

        int configVersion = PreferencesHelperDataplan.on().getConfigVersion();

        if (configurationCommand != null && configVersion < configurationCommand.getConfigVersionCode()) {

            PreferencesHelperDataplan.on().setConfigVersion(configurationCommand.getConfigVersionCode());
            PreferencesHelperDataplan.on().setPerMbTokenValue(configurationCommand.getTokenPerMb());

            PreferencesHelperDataplan.on().setMaxPointForRmesh(configurationCommand.getMaxPointForRmesh());
            PreferencesHelperDataplan.on().setRmeshPerPoint(configurationCommand.getRmeshPerToken());
            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_LINK, configurationCommand.getGiftDonateLink());

            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_USERNAME, configurationCommand.getGiftDonateLink());
            SharedPref.write(Constant.PreferenceKeys.GIFT_DONATE_PASS, configurationCommand.getGiftDonatePass());
            SharedPref.write(Constant.PreferenceKeys.TX_HISTORY_URL_KOTTI, configurationCommand.getHistoryUrlKotti());
            SharedPref.write(Constant.PreferenceKeys.TX_URL_ROPSTEN, configurationCommand.getRopstenUrl());


            PreferencesHelperDataplan.on().setWalletRmeshAvailable(configurationCommand.isWalletRmeshAvailable());
            PreferencesHelperDataplan.on().setRmeshInfoText(configurationCommand.getRmeshInfoText());
            PreferencesHelperDataplan.on().setRmeshOwnerAddress(configurationCommand.getRmeshOwnerAddress());
            PreferencesHelperDataplan.on().setMainnetNetworkType(configurationCommand.getMainNetNetworkType());

            for (Network network : configurationCommand.getNetwork()) {
                EthereumServiceUtil.getInstance(context).insertNetworkInfo(new NetworkInfo().toNetworkInfo(network));
            }

            EthereumServiceUtil.getInstance(context).getEthereumService().setGIftDonateUrl(configurationCommand.getGiftDonateLink());
        }
    }

/*    private String loadJSONFromAsset(Context context) {
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

    }*/

    private void processGuidelineJson(Context context, String guidelineJson) {
        //PointGuideLine tokenGuideLine = new Gson().fromJson(guidelineJson, PointGuideLine.class);

        FileStoreUtil.writeTokenGuideline(context, guidelineJson);
        // writing html file but now off
        //FileStoreUtil.writeWebFile(context, tokenGuideLine.getContent());
    }

    private class DownloadGuidelineContent extends AsyncTask<String, Void, String> {
        private Context context;

        public DownloadGuidelineContent(Context context) {
            this.context = context;
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

                String authString = (userName + ":" + userPass);
                byte[] data1 = authString.getBytes(UTF_8);
                String base64 = Base64.encodeToString(data1, Base64.NO_WRAP);


               /* Log.e("HttpError", "Credential " +userName+" password: "+userPass);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, userPass.toCharArray());
                    }
                });*/

                connection.setRequestProperty("Authorization", "Basic " + base64);

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

            processGuidelineJson(context, s);
        }
    }
}
