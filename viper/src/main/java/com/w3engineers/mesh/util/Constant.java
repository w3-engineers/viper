package com.w3engineers.mesh.util;

import android.graphics.Color;
import android.os.Environment;

import java.util.UUID;

public class Constant {

    public interface IntentKey{
        String SSID = "ssid";
    }

    public static String RANDOM_STATE = "mesh_random_state";
    public static String KEY_DEVICE_SSID_NAME = "mesh_ssid_name";
    public static String KEY_DEVICE_BLE_NAME = "mesh_device_ble";
    public static String KEY_USER_ID = "mesh_id";
    public static String KEY_USER_NAME = "mesh_name";
    public static String KEY_BLE_PREFIX = "mesh_ble_prefix";
    public static String RECENT_IMAGE_PATH = "mesh_recent_image_path";

    public static String CURRENT_LOG_FILE_NAME;


    public static String KEY_MULTIVERSE_URL = "mesh_multiverse_url";
    public static final String NAME_INSECURE = "mesh_BluetoothChatInsecure";

    // Unique UUID for this application
    public static final UUID MY_UUID_INSECURE = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static String MASTER_IP_ADDRESS = "192.168.49.1";
    public static String P2P_IP_ADDRESS_PREFIX = "192.168.49.";

    public static String KEY_DEVICE_AP_MODE = "mesh_device_ap_mode";

    public static String KEY_NETWORK_PREFIX = "mesh_network_prefix";

    public static String LATEST_UPDATE = "latest_update";
    public static long SELLER_MINIMUM_WARNING_DATA = 1024; // The format is byte
    public static String AUTO_IMAGE_CAPTURE = "auto_image_capture";



    public interface SellerStatus {
        int PURCHASE = 0;
        int PURCHASING = 1;
        int PURCHASED = 2;
        int CONNECTING = 3;
        int CONNECTED = 4;
        int DISCONNECT = 5;
        int DISCONNECTING = 6;
        int DISCONNECTED = 7;
        int CLOSE = 8;
        int CLOSING = 9;
        int CLOSED = 10;
    }

    public interface MessageStatus {
        int SENDING = 0;
        int SEND = 1;
        int DELIVERED = 2;
        int RECEIVED = 3;
        int FAILED = 4;
    }


    public interface UserTpe {
        int WIFI = 1;
        int BLUETOOTH = 2;
        int INTERNET = 3;
    }

    public interface DataType {
        int USER_LIST = 1;
        int USER_MESSAGE = 2;
    }

    public interface IntentKeys {
        String NUMBER_OF_ACTIVE_BUYER = "num_of_active_buyer";
    }

    public interface PreferenceKeys {
        String ADDRESS = "eth_address";
        String ADDRESS_BITMAP = "address_bitmap";
    }


    public interface TimeoutPurpose {
        int INIT_PURCHASE = 6;
        int INIT_CHANNEL = 7;
        int INIT_ETHER = 8;
    }

    public interface DiagramColor {
        int[] colors = new int[]{Color.DKGRAY,
                Color.rgb(0, 102, 0),
                Color.rgb(0, 0, 204),
                Color.rgb(0, 204, 0),
                Color.rgb(100, 100, 255),
                Color.rgb(77, 0, 64),
                Color.rgb(179, 0, 149),
                Color.rgb(230, 0, 0),
                Color.LTGRAY};
        String[] colorDeffs = new String[]{"Disconnected",
                "WiFi Direct",
                "BLE Direct",
                "WiFi Mesh",
                "BLE Mesh",
                "Internet Direct",
                "Internet Mesh",
                "Connected Link",
                "Disconnected Link"
        };
    }

    public interface Directory {
        String PARENT_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/Telemesh/";
        String NETWORK_IMAGES = "/NetworkImages/";
        String MESH_LOG = "/MeshLog/";
        String MESH_SDK_CRUSH = "/MeshSDKCrash/";
        String MESH_ID = "/MeshId/";
        String FILE_NO_MEDIA = "/.nomedia/";

        int MAXIMUM_IMAGE = 50;
    }

    public interface DiagramId {
        int MAIN_LAYOUT_ID = 123450;
        int CENTER_LAYOUT_ID = 123451;
        int MY_NODE_ID = 123452;
        int SHOW_SCREENSHOT_ID = 123453;
        int DOT_BUTTON_ID = 123454;
        int DELETE_BUTTON_ID = 123455;
        int LINE_DRAW_ID = 123456;
        int INNER_LAYOUT_ID = 123457;

        int BOTTOM_NAVIGATION_HEIGHT = 280;
    }

    public interface Cycle {
        /**
         * To remove from temporary exception list and remove node from database if not updated with
         * new path
         */
        int DISCONNECTION_MESSAGE_TIMEOUT = 10 * 1000;

        int MESH_USER_DELAY_THRESHOLD = 3 * 1000;
    }
}
